import React from "react";

import onlineIcon from "../icons/onlineIcon.png";
import closeIcon from "../icons/closeIcon.png";

import "./InfoBar.css";
import { Link } from "react-router-dom";

const InfoBar = ({ room }) => (
  <div className="chat_infoBar">
    <div className="chat_leftInnerContainer">
      <img className="chat_onlineIcon" src={onlineIcon} alt="online icon" />
      <h3>{room}</h3>
    </div>
    <div className="chat_rightInnerContainer">
      <Link to="/joinchat">
        <img src={closeIcon} alt="close icon" />
      </Link>
    </div>
  </div>
);

export default InfoBar;
