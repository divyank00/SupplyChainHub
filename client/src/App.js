import { createMuiTheme, MuiThemeProvider } from "@material-ui/core";
import React, { useEffect, useState } from "react";
import "./App.css";
import Navbar from "./components/Navbar";
import Routes from "./Routes";
import Web3 from "web3";

function App() {
  const THEME = createMuiTheme({
    typography: {
      fontFamily: `"Poppins", sans-serif`,
      fontSize: 14,
      fontWeightLight: 300,
      fontWeightRegular: 400,
      fontWeightMedium: 500,
    },
    palette: {
      type: "dark",
      primary: {
        main: "#7e89fd",
      },
      background: {
        default: "#ffffff",
        paper: "#202225",
      },
    },
  });

  const [balance, setBalance] = useState(0);

  return (
    <MuiThemeProvider theme={THEME}>
      <div className="App">
        <Navbar />
        <Routes />
      </div>
    </MuiThemeProvider>
  );
}

export default App;
