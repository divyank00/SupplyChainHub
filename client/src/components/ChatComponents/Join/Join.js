import Avatar from "@material-ui/core/Avatar";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import ListItemAvatar from "@material-ui/core/ListItemAvatar";
import ListItemText from "@material-ui/core/ListItemText";
import { makeStyles } from "@material-ui/core/styles";
import ImageIcon from "@material-ui/icons/AccountCircle";
import { motion } from "framer-motion";
import React, { useState } from "react";
import { useHistory } from "react-router-dom";
import "./Join.css";

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
  const [members, setMembers] = useState([
    { name: "Shubham Kukreja", role: "Manufacturer" },
    { name: "Khushi Asawa", role: "Dealer" },
    { name: "Rishi Gondkar", role: "Distributor" },
    { name: "Divyank Lunkad", role: "Retailer" },
    { name: "Madhura Kunjir", role: "Manufacturer" },
  ]);

  const classes = useStyles();
  const history = useHistory();

  const joinRoom = (member) => {
    const name = localStorage.getItem("owner_name") || "ShubhamK";
    const room =
      member.name < name ? `${name}_${member.name}` : `${member.name}_${name}`;
    history.push(`/chat?name=${name}&room=${room}`, { roomName: member.name });
  };

  return (
    <div className="chat_joinOuterContainer">
      <motion.List
        className={classes.root}
        initial={{ y: 100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, type: "tween", staggerChildren: 1 }}
      >
        {members.map((member) => (
          <ListItem
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
