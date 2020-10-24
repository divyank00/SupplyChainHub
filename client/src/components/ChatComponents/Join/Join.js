import Avatar from "@material-ui/core/Avatar";
import ListItem from "@material-ui/core/ListItem";
import ListItemAvatar from "@material-ui/core/ListItemAvatar";
import ListItemText from "@material-ui/core/ListItemText";
import { makeStyles } from "@material-ui/core/styles";
import ImageIcon from "@material-ui/icons/AccountCircle";
import { motion } from "framer-motion";
import React, { useEffect, useState } from "react";
import { useHistory } from "react-router-dom";
import Web3 from "web3";
import firebase from "../../../firebase";
import "./Join.css";
import CircularProgress from "@material-ui/core/CircularProgress";
const useStyles = makeStyles((theme) => ({
  root: {
    width: "100%",
    maxWidth: 360,
    backgroundColor: theme.palette.background.paper,
    boxShadow: "10px 10px 50px rgba(0,0,0,0.16)",
    borderRadius: 20,
    padding: 20,
  },
}));
export default function SignIn() {
  const [contractAddress, setContractAddress] = useState("");
  const [contract, setContract] = useState();
  const [members, setMembers] = useState([]);

  const classes = useStyles();
  const history = useHistory();

  const joinRoom = (member) => {
    const name = localStorage.getItem("owner_name") || "ShubhamK";
    const room =
      member.name < name ? `${name}_${member.name}` : `${member.name}_${name}`;
    history.push(`/chat?name=${name}&room=${room}`, { roomName: member.name });
  };

  const getAbi = async () => {
    const docRef = firebase
      .firestore()
      .collection("User")
      .doc("0xc55e839ed3a2c6f1053d7af10f3abfa2adf2d903")
      .collection("Contracts");

    const snapshot = await docRef.get();
    let a = [];
    snapshot.forEach((doc) => {
      a.push(doc.data());
    });
    setContractAddress(a[0].address);

    const docRef2 = firebase
      .firestore()
      .collection("Contracts")
      .doc(a[0].address);
    const doc = await docRef2.get();

    window.web3 = new Web3("https://rpc-mumbai.matic.today");
    await window.ethereum.enable();
    const web3 = window.web3;
    const temp_contract = new web3.eth.Contract(
      JSON.parse(doc.data().abi),
      a[0].address
    );
    setContract(temp_contract);
  };

  const loadMembers = async () => {
    if (contract) {
      const roles = await contract.methods.getUserRolesArray().call();
      const res = await contract.methods.ReturnAllUsers().call();
      res.map((address) => {
        contract.methods
          .getUserDetails(address)
          .call()
          .then((res) => {
            setMembers((members) => [
              ...members,
              { name: res[1], role: roles[res[0]] },
            ]);
          });
      });
    }
  };

  useEffect(() => {
    getAbi();
  }, []);

  useEffect(() => {
    loadMembers();
  }, [contract, contractAddress]);

  if (!contract) {
    return (
      <div className="chat_joinOuterContainer">
        <CircularProgress />
      </div>
    );
  }

  return (
    <div className="chat_joinOuterContainer">
      <motion.List
        className={classes.root}
        initial={{ y: 100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, type: "tween", staggerChildren: 1 }}
      >
        {members.map((member, index) => (
          <ListItem
            key={index}
            style={{
              cursor: "pointer",
            }}
            onClick={() => joinRoom(member)}
          >
            <ListItemAvatar>
              <Avatar style={{ backgroundColor: "white" }}>
                <ImageIcon color="primary" />
              </Avatar>
            </ListItemAvatar>
            <ListItemText primary={member.name} secondary={member.role} />
          </ListItem>
        ))}
      </motion.List>
    </div>
  );
}
