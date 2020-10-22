import React from "react";

import onlineIcon from "../icons/onlineIcon.png";

import "./TextContainer.css";

const TextContainer = ({ users }) => (
  <div className="chat_textContainer">
    {users ? (
      <div>
        <div className="chat_activeContainer">
          <h2>
            {users.map(({ name }) => (
              <div key={name} className="chat_activeItem">
                {name}
                <img alt="Online Icon" src={onlineIcon} />
              </div>
            ))}
          </h2>
        </div>
      </div>
    ) : null}
  </div>
);

export default TextContainer;
