import React, { useEffect, useState } from "react";
import CeramicClient from "@ceramicnetwork/ceramic-http-client";
import "./Message.css";
import { motion } from "framer-motion";
import { ceramic } from "../../Chat/Chat";

const Message = ({ message: { text, user }, name }) => {
  const [textContent, setTextContent] = useState("Loading..");

  let isSentByCurrentUser = false;

  const trimmedName = name.trim().toLowerCase();

  if (user === trimmedName) {
    isSentByCurrentUser = true;
  }

  const getText = async (id) => {
    const doc = await ceramic.loadDocument(id);
    setTextContent(doc.content.desc);
  };

  useEffect(() => {
    getText(text);
  }, [text]);

  return (
    <motion.div
      className="row"
      initial={{ y: 100, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.5, type: "tween", staggerChildren: 1 }}
    >
      <div
        className={!isSentByCurrentUser ? "dialogue d-bot" : "dialogue d-human"}
      >
        <div className="msg-author">{user}</div>
        <div className="msg-content">{textContent}</div>
      </div>
    </motion.div>
  );
};

export default Message;
