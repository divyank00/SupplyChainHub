import React, { useState } from "react";
import InputComponent from "./InputComponent";
import ConversationBox from "./ConversationBox";
import { Container } from "@material-ui/core";

function MainScreen() {
  const [conversation, setConversation] = useState([]);

  const handleSave = async () => {
    setConversation([]);
  };

  const reset = () => {
    setConversation([]);
  };

  return (
    <Container>
      <ConversationBox
        conversations={conversation}
        setConversation={setConversation}
      />
      <InputComponent
        conversations={conversation}
        setConversation={setConversation}
        handleSave={handleSave}
        reset={reset}
      />
    </Container>
  );
}

export default MainScreen;
