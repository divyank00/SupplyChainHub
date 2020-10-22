import CeramicClient from "@ceramicnetwork/ceramic-http-client";
import { motion } from "framer-motion";
import IdentityWallet from "identity-wallet";
import queryString from "query-string";
import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import io from "socket.io-client";
import * as u8a from "uint8arrays";
import InfoBar from "../InfoBar/InfoBar";
import Input from "../Input/Input";
import Messages from "../Messages/Messages";
import "./Chat.css";

const ceramic = new CeramicClient();

let socket;

const Chat = (props) => {
  const [name, setName] = useState("");
  const [room, setRoom] = useState("");
  const [users, setUsers] = useState("");
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  let location = useLocation();

  const createDoc = async (msg) => {
    const seed = u8a.fromString(
      "8e641c0dc77f6916cc7f743dad774cdf9f6f7bcb880b11395149dd878377cd398650bbfd4607962b49953c87da4d7f3ff247ed734b06f96bdd69479377bc612b",
      "base16"
    );
    try {
      const idw = await IdentityWallet.create({
        getPermission: async () => [],
        seed: seed,
      });
      await ceramic.setDIDProvider(idw.getDidProvider());
      const doc1 = await ceramic.createDocument("tile", {
        content: { title: "Client Document", desc: msg },
      });

      return doc1.id;
    } catch (err) {
      alert(err);
    }
  };

  useEffect(() => {
    const { name, room } = queryString.parse(location.search);
    socket = io("http://localhost:5000");
    setRoom(room);
    setName(name);
    socket.emit("join", { name, room }, (error) => {
      if (error) {
        alert(error);
      }
    });
  }, [location]);

  useEffect(() => {
    socket.on("message", (message) => {
      setMessages((messages) => [...messages, message]);
    });

    socket.on("roomData", ({ users }) => {
      setUsers(users);
    });
  }, []);

  const docId = async (msg) => {
    setLoading(true);
    const doc = await createDoc(msg);
    setLoading(false);
    return doc;
  };

  const sendMessage = async (event) => {
    event.preventDefault();

    docId(message).then((doc) => {
      socket.emit("sendMessage", doc, () => setMessage(""));
    });
  };

  return (
    <div className="chat_outerContainer">
      <motion.div
        className="chat_container"
        initial={{ x: 500, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.2, type: "tween" }}
      >
        <InfoBar room={location.state.roomName} />
        <Messages messages={messages} name={name} />
        <Input
          message={message}
          setMessage={setMessage}
          sendMessage={sendMessage}
          loading={loading}
        />
      </motion.div>
      {/* <TextContainer users={users} /> */}
    </div>
  );
};

export default Chat;
