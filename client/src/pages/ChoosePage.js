import { Container, makeStyles, Typography } from "@material-ui/core";
import AccountBalanceWalletIcon from "@material-ui/icons/AccountBalanceWallet";
import AddIcon from "@material-ui/icons/Add";
import ChatIcon from "@material-ui/icons/Chat";
import SendIcon from "@material-ui/icons/Send";
import { motion } from "framer-motion";
import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Web3 from "web3";

const useStyles = makeStyles((theme) => ({
  blockPaper: {
    display: "inline-flex",
    height: "10rem",
    width: "10rem",
    alignItems: "center",
    justifyContent: "center",
    margin: "2rem",
    flexDirection: "column",
    padding: "5rem",
    borderRadius: "25px",
    cursor: "pointer",

    backgroundColor: theme.palette.background.paper,
  },
  blockIicon: {
    fontWeight: 600,
    fontSize: "5rem",
  },
  overlay: {
    width: "50%",
    height: "50vh",
    backgroundColor: theme.palette.primary.main,
    position: "absolute",
    top: 0,
    left: 0,
    zIndex: -1000000,
  },
}));

function ChoosePage() {
  const classes = useStyles();
  const [accountAddress, setaccountAddress] = useState("");

  const connectWallet = async () => {
    console.log("Connecting Wallet");
    if (window.ethereum) {
      window.web3 = new Web3(window.ethereum);
      await window.ethereum.enable();
      const web3 = window.web3;
      const accounts = await web3.eth.getAccounts();
      setaccountAddress(accounts[0]);
      localStorage.setItem("account_address", accounts[0]);
    } else {
      window.alert("Please Install MetaMask.");
    }
  };

  if (!accountAddress) {
    return (
      <Container
        style={{
          textAlign: "center",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "80vh",
          zIndex: 1,
        }}
      >
        <motion.Paper
          initial={{ y: 100, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ duration: 0.5, type: "tween" }}
          whileHover={{ scale: 1.05 }}
          className={classes.blockPaper}
          elevation={0}
          onClick={connectWallet}
        >
          <AccountBalanceWalletIcon
            className={classes.blockIicon}
            color="primary"
          />
          <br />
          <br />
          <Typography variant="h4">Connect</Typography>
          <Typography color="primary">MetaMask Wallet</Typography>
        </motion.Paper>
      </Container>
    );
  }

  return (
    <>
      <Container
        style={{
          textAlign: "center",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "80vh",
          zIndex: 1,
        }}
      >
        <Link style={{ textDecoration: "none", color: "white" }} to="/build">
          <motion.Paper
            initial={{ y: 100, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ duration: 0.5, type: "tween" }}
            whileHover={{ scale: 1.05 }}
            className={classes.blockPaper}
            elevation={0}
          >
            <AddIcon className={classes.blockIicon} color="primary" />
            <br />
            <br />
            <Typography variant="h4">Create</Typography>
            <Typography variant="p">New Supply Chain</Typography>
          </motion.Paper>
        </Link>
        <Link style={{ textDecoration: "none", color: "white" }} to="/join">
          <motion.Paper
            initial={{ y: 100, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ duration: 0.5, type: "tween", delay: 0.25 }}
            className={classes.blockPaper}
            whileHover={{ scale: 1.05 }}
            elevation={0}
          >
            <SendIcon className={classes.blockIicon} color="primary" />
            <br />
            <br />
            <Typography variant="h4">Join</Typography>
            <Typography variant="p">a Supply Chain </Typography>
          </motion.Paper>
        </Link>
        <Link style={{ textDecoration: "none", color: "white" }} to="/joinchat">
          <motion.Paper
            initial={{ y: 100, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ duration: 0.5, type: "tween", delay: 0.5 }}
            className={classes.blockPaper}
            whileHover={{ scale: 1.05 }}
            elevation={0}
          >
            <ChatIcon className={classes.blockIicon} color="primary" />
            <br />
            <br />
            <Typography variant="h4">Connect</Typography>
            <Typography variant="p">with Others </Typography>
          </motion.Paper>
        </Link>
      </Container>
    </>
  );
}

export default ChoosePage;
