import React, { useEffect, useState } from "react";
import { makeStyles } from "@material-ui/core/styles";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Typography from "@material-ui/core/Typography";
import Button from "@material-ui/core/Button";
import IconButton from "@material-ui/core/IconButton";
import MenuIcon from "@material-ui/icons/Menu";
import { Link } from "react-router-dom";

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
  },
  menuButton: {
    marginRight: theme.spacing(2),
  },
  title: {
    flexGrow: 1,
  },
  navbar: {
    backdropFilter: "blur(10px)",
    boxShadow: "10px 10px 25px rgba(0,0,0,0.16)",
  },
}));

export default function Navbar() {
  const classes = useStyles();
  const [accountAddress, setAccountAddress] = useState("");

  useEffect(() => {
    setAccountAddress(localStorage.getItem("account_address"));
  }, [localStorage]);

  return (
    <div className={classes.root}>
      <AppBar position="static" className={classes.navbar} elevation={0}>
        <Toolbar>
          <IconButton
            edge="start"
            className={classes.menuButton}
            color="inherit"
            aria-label="menu"
          >
            <MenuIcon />
          </IconButton>

          <Typography variant="h6" className={classes.title}>
            <Link style={{ textDecoration: "none", color: "white" }} to="/home">
              Supply Chain Hub
            </Link>
          </Typography>

          <Button color="inherit">{accountAddress}</Button>
        </Toolbar>
      </AppBar>
    </div>
  );
}
