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
import firebase from "../../../firebase";
import { defaultseed } from "../../../constants/keys/IdentityWalletSeed";
export const ceramic = new CeramicClient();

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
    try {
      const newMessage = await ceramic.createDocument("tile", {
        content: {
          title: "Message",
          desc: msg,
          user: localStorage.getItem("owner_name").trim().toLowerCase(),
        },
      });
      const docRef = firebase
        .firestore()
        .collection("Chat")
        .doc(room.replace(/ /g, ""));

      const doc = await docRef.get();

      let docData = newMessage._state.content;
      docData.text = newMessage.id;

      if (!doc.data()) {
        const data = { msg: [JSON.stringify(docData)] };
        await firebase
          .firestore()
          .collection("Chat")
          .doc(room.replace(/ /g, ""))
          .set(data);
      } else {
        let newMsg = [...doc.data().msg, JSON.stringify(docData)];
        await docRef.update({
          msg: newMsg,
        });
      }
      return newMessage.id;
    } catch (err) {
      alert(err);
    }
  };

  const initCeramic = async () => {
    const seed = u8a.fromString(defaultseed, "base16");
    const idw = await IdentityWallet.create({
      getPermission: async () => [],
      seed: seed,
    });
    setLoading(true);
    await ceramic.setDIDProvider(idw.getDidProvider());
    setLoading(false);
  };

  const getMessages = async () => {
    const { room } = queryString.parse(location.search);
    const docRef = firebase
      .firestore()
      .collection("Chat")
      .doc(room.replace(/ /g, ""));

    const doc = await docRef.get();
    if (!doc.data()) {
      setMessages([]);
      return;
    }
    doc.data().msg.forEach((item) => {
      setMessages((messages) => [...messages, JSON.parse(item)]);
    });
  };

  useEffect(() => {
    const { name, room } = queryString.parse(location.search);
    socket = io("http://localhost:5000");
    setRoom(room);
    setName(name);
    socket.emit("join", { name, room }, (error) => {
      if (error) {
        if (error === "Username is taken.") return;
        alert(error);
      }
    });
    initCeramic();
  }, [location.search]);

  useEffect(() => {
    socket.on("message", (message) => {
      setMessages((messages) => [...messages, message]);
    });

    socket.on("roomData", ({ users }) => {
      setUsers(users);
    });
    getMessages();
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
    </div>
  );
};

export default Chat;
