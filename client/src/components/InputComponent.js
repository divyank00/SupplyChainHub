import React, { Component } from "react";
import { TextField, IconButton } from "@material-ui/core";
import { HUMAN, BOT } from "../constants";
import "../App.css";
import SpeechRecognition from "react-speech-recognition";
import MicIcon from "@material-ui/icons/Mic";
import InputAdornment from "@material-ui/core/InputAdornment";
import SendIcon from "@material-ui/icons/SendRounded";

class InputComponent extends Component {
  state = {
    value: "",
    listening: false,
    modalOpen: false,
  };

  textInput = React.createRef();

  handleChange = (e) => {
    this.setState({
      value: e.target.value,
    });
  };

  handleClick = (type) => {
    this.props.setConversation((prev) => [
      ...prev,
      {
        value: this.state.value,
        author: type ? HUMAN : BOT,
        time: new Date().getTime(),
      },
    ]);
    this.props.resetTranscript(true);
    this.setState({
      value: "",
    });
    this.textInput.current.focus();
  };

  handleListen = () => {
    this.setState({
      listening: !this.state.listening,
    });
    if (!this.state.listening) {
      this.props.startListening(true);
    } else {
      this.props.stopListening(false);
    }
  };

  componentWillReceiveProps() {
    this.setState({
      value: this.props.transcript,
    });
  }

  render() {
    const { browserSupportsSpeechRecognition } = this.props;

    if (!browserSupportsSpeechRecognition) {
      return null;
    }

    return (
      <div
        style={{
          display: "flex",
          justifyContent: "space-evenly",
          alignItems: "center",
        }}
      >
        <TextField
          variant="outlined"
          label="Enter Text Here"
          value={this.state.value}
          onChange={this.handleChange}
          multiline
          autoFocus
          className="input"
          inputRef={this.textInput}
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton onClick={this.handleListen}>
                  {!this.state.listening ? (
                    <MicIcon />
                  ) : (
                    <MicIcon color="primary" />
                  )}
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
        <IconButton
          style={{
            padding: "1rem",
            backgroundColor: "#7e89fd",
            color: "white",
          }}
          onClick={() => this.handleClick(1)}
        >
          <SendIcon />
        </IconButton>
      </div>
    );
  }
}
const options = {
  autoStart: false,
};

export default SpeechRecognition(options)(InputComponent);
