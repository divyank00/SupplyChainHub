import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Button,
  makeStyles,
  TextField,
  Typography,
} from "@material-ui/core";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import React, { useEffect, useState } from "react";
import Dialog from "@material-ui/core/Dialog";
import DialogActions from "@material-ui/core/DialogActions";
import DialogContent from "@material-ui/core/DialogContent";
import DialogContentText from "@material-ui/core/DialogContentText";
import DialogTitle from "@material-ui/core/DialogTitle";

const useStyles = makeStyles((theme) => ({
  root: {
    width: "100%",
  },
  heading: {
    fontSize: theme.typography.pxToRem(15),
    flexBasis: "33.33%",
    flexShrink: 0,
  },
  secondaryHeading: {
    fontSize: theme.typography.pxToRem(15),
    color: theme.palette.text.secondary,
  },
}));

function FunctionBlock({ constant, name, inputs, payable, type, contract }) {
  const classes = useStyles();
  const [expanded, setExpanded] = React.useState(false);
  const [state, setState] = useState([]);
  const [open, setOpen] = React.useState(false);
  const [result, setResult] = useState();

  const handleClickOpen = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
  };

  useEffect(() => {}, []);

  const handleChange = (panel) => (event, isExpanded) => {
    setExpanded(isExpanded ? panel : false);
  };

  const callFunction = async () => {
    const res = await contract.methods[name]().call();
    setOpen(true);
    setResult(res);
  };

  const sendFunction = () => {};

  if (type !== "function") return <></>;
  return (
    <>
      <Accordion
        expanded={expanded === "panel1"}
        onChange={handleChange("panel1")}
        style={{ padding: "1rem" }}
      >
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          aria-controls="panel1bh-content"
          id="panel1bh-header"
        >
          <Typography className={classes.heading}>{name}</Typography>
          <Typography className={classes.secondaryHeading}>{type}</Typography>
        </AccordionSummary>
        <AccordionDetails>
          <div>
            <div>
              {inputs.map((input, index) => (
                <TextField
                  label={input.name}
                  key={index}
                  style={{ margin: "1rem" }}
                  placeholder={input.type}
                />
              ))}
              <Button
                variant="contained"
                color="primary"
                style={{ margin: "1rem" }}
                onClick={constant ? callFunction : sendFunction}
              >
                {constant ? "Call" : "Send"}
              </Button>
            </div>
          </div>
        </AccordionDetails>
      </Accordion>
      <Dialog
        open={open}
        onClose={handleClose}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">{name}()</DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Function <b>{name}()</b> returned: {result}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} color="primary">
            Ok
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

export default FunctionBlock;
