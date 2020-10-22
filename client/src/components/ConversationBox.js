import React, { useRef, useEffect } from "react";
import { HUMAN } from "../constants";
import "../App.css";

function ConversationBox(props) {
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(scrollToBottom, [props.conversations]);

  return (
    <div className="main-container">
      <div className="header">{new Date().toDateString()}</div>
      <div className="dialogue-container">
        {props.conversations.map((item, index) => (
          <div className="row" key={index}>
            <div
              className={
                item.author === HUMAN ? "dialogue d-human" : "dialogue d-bot"
              }
            >
              <div className="msg-author">
                {item.author === HUMAN ? "Human" : "Bot"}
              </div>
              <div className="msg-content">{item.value}</div>
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
    </div>
  );
}

export default ConversationBox;
