import { Button, Container, TextField } from "@material-ui/core";
import React, { useEffect, useState } from "react";
import Web3 from "web3";
import FunctionBlock from "../components/FunctionBlock";
import firebase from "../firebase";

function InteractPage() {
  const [contractAddress, setContractAddress] = useState("");
  const [abi, setAbi] = useState([]);
  const [contract, setContract] = useState();

  const getAbi = async () => {
    const docRef = firebase
      .firestore()
      .collection("Contracts")
      .doc(contractAddress);
    const doc = await docRef.get();
    if (!doc.exists) {
      alert("InValid Contract Address");
    } else {
      setAbi(JSON.parse(doc.data().abi));
      window.web3 = new Web3('https://rpc-mumbai.matic.today');
      await window.ethereum.enable();
      const web3 = window.web3;
      const temp_contract = new web3.eth.Contract(
        JSON.parse(doc.data().abi),
        contractAddress
      );
      setContract(temp_contract);
    }
  };

  return (
    <Container>
      <br />
      <br />
      <br />
      <br />
      <div
        style={{
          display: "flex",
          width: "100%",
          justifyContent: "space-around",
        }}
      >
        <TextField
          style={{ flex: 0.8 }}
          onChange={(e) => setContractAddress(e.target.value)}
          value={contractAddress}
          id="outlined-basic1"
          label="Contract Address"
          variant="outlined"
          required
          autoFocus
        />
        <Button variant="contained" color="primary" onClick={getAbi}>
          Enter
        </Button>
      </div>
      <br />
      <br />
      <br />

      {abi.length ? (
        <Container>
          {abi.map((func, index) => (
            <FunctionBlock contract={contract} {...func} key={index} />
          ))}
        </Container>
      ) : (
        <></>
      )}
    </Container>
  );
}

export default InteractPage;
