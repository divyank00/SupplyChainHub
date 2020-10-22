import { IconButton, TextField } from "@material-ui/core";
import CachedIcon from "@material-ui/icons/Cached";
import SendIcon from "@material-ui/icons/SendRounded";
import React from "react";
import "./Input.css";
const Input = ({ setMessage, sendMessage, message, loading }) => {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: 20,
      }}
    >
      <TextField
        className="chat_input"
        type="text"
        placeholder="Type a message..."
        value={message}
        variant="outlined"
        onChange={({ target: { value } }) => setMessage(value)}
        onKeyPress={(event) =>
          event.key === "Enter" ? sendMessage(event) : null
        }
      />
      <IconButton
        style={{
          padding: "1rem",
          backgroundColor: "#7e89fd",
          color: "white",
        }}
        onClick={(e) => sendMessage(e)}
      >
        {loading ? <CachedIcon /> : <SendIcon />}
      </IconButton>
    </div>
  );
};

export default Input;
