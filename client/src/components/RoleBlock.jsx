import {
  AppBar,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  IconButton,
  TextField,
  Toolbar,
  Typography,
} from "@material-ui/core";
import Accordion from "@material-ui/core/Accordion";
import AccordionDetails from "@material-ui/core/AccordionDetails";
import AccordionSummary from "@material-ui/core/AccordionSummary";
import Fab from "@material-ui/core/Fab";
import Slide from "@material-ui/core/Slide";
import { makeStyles } from "@material-ui/core/styles";
import AddIcon from "@material-ui/icons/Add";
import CloseIcon from "@material-ui/icons/Close";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import { motion } from "framer-motion";
import React, { useEffect, useState } from "react";
import { functions } from "../constants/functions/roleFunctions";

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide direction="up" ref={ref} {...props} />;
});

const useStyles = makeStyles((theme) => ({
  paper: {
    height: 50,
    minWidth: 120,
    justifyContent: "center",
    alignItems: "center",
    display: "inline-flex",
    paddingLeft: theme.spacing(4),
    paddingRight: theme.spacing(4),
    paddingTop: theme.spacing(2),
    paddingBottom: theme.spacing(2),
    backgroundColor: theme.palette.background.paper,
    border: `2px solid ${theme.palette.primary.main}`,
    color: theme.palette.primary.main,
    boxShadow: "0 5px 20px rgba(0,0,0,.16)",
    zIndex: 0,
  },
  arrowContainer: {
    display: "inline-flex",
    justifyContent: "center",
    alignItems: "center",
    width: 75,
  },
  arrow: {
    backgroundColor: theme.palette.primary.main,
    height: 2,
    width: "100%",
    zIndex: -1,
  },
  appBar: {
    position: "relative",
  },
  title: {
    marginLeft: theme.spacing(2),
    flex: 1,
  },
  heading: {
    fontSize: theme.typography.pxToRem(15),
    flexBasis: "33.33%",
    flexShrink: 0,
    fontWeight: 500,
  },
}));

function RoleBlock(props) {
  const classes = useStyles();
  const [open, setOpen] = useState(false);
  const [expanded, setExpanded] = React.useState(false);
  const [type, setType] = useState(0);
  const [nextRole, setNextRole] = useState("");
  const [prevRole, setPrevRole] = useState("");

  useEffect(() => {
    if (props?.roles?.indexOf(props.name) === 0) {
      setType(0);
      setNextRole(props.roles?.[props?.roles?.indexOf(props.name) + 1]);
      setPrevRole(0);
    } else if (props?.roles?.indexOf(props.name) === props?.roles?.length - 1) {
      setType(2);
      setNextRole(0);
      setPrevRole(props.roles?.[props?.roles?.indexOf(props.name) - 1]);
    } else {
      setType(1);
      setNextRole(props.roles?.[props?.roles?.indexOf(props.name) + 1]);
      setPrevRole(props.roles?.[props?.roles?.indexOf(props.name) - 1]);
    }
  }, [props]);

  const [roleFunctions, setroleFunctions] = useState([]);

  useEffect(() => {
    setroleFunctions(functions(props.name, nextRole, prevRole, type));
  }, [props, nextRole, prevRole, type]);

  const handleOpen = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
  };

  const removeRole = () => {
    props.removeRole(props.name);
    setOpen(false);
  };

  const handleChangeAccordian = (panel) => (event, isExpanded) => {
    setExpanded(isExpanded ? panel : false);
  };

  const changeRoleName = (newName) => {
    const newRoles = props.roles.map((item) => {
      if (item === props.name) return newName;
      else return item;
    });
    props.setRoles(newRoles);
  };

  if (props.blank) {
    return (
      <div
        onClick={props.handleClickOpen}
        style={{
          textAlign: "center",
          width: "100%",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <Fab
          color="primary"
          aria-label="add"
          variant="extended"
          style={{ boxShadow: "none" }}
        >
          <AddIcon />
          Add Role &nbsp;
        </Fab>
      </div>
    );
  }

  return (
    <>
      <motion.div
        initial={{ x: -200, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        whileHover={{ scale: 1.05 }}
        transition={{ type: "tween", delay: 0.2 }}
        style={{
          display: "inline-flex",
          flexDirection: "row",
          cursor: "pointer",
        }}
        onClick={handleOpen}
      >
        <div className={classes.paper}>{props.name}</div>
        {!props.lastElement ? (
          <div className={classes.arrowContainer}>
            <div className={classes.arrow}></div>
          </div>
        ) : (
          <></>
        )}
      </motion.div>
      <Dialog
        open={open}
        onClose={handleClose}
        aria-labelledby="form-dialog-title"
        TransitionComponent={Transition}
        keepMounted
      >
        <AppBar className={classes.appBar}>
          <Toolbar>
            <IconButton
              edge="start"
              color="inherit"
              onClick={handleClose}
              aria-label="close"
            >
              <CloseIcon />
            </IconButton>
            <Typography variant="h6" className={classes.title}>
              {props.name}
            </Typography>
            <Button onClick={removeRole} color="secondary" variant="contained">
              Delete Role
            </Button>
          </Toolbar>
        </AppBar>
        <DialogTitle id="form-dialog-title">Edit {props.name}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Slightest Change in each role will affect the whole supply chain.
          </DialogContentText>
          <TextField
            onChange={(event) => changeRoleName(event.target.value)}
            label="Role Name"
            type="text"
            margin="dense"
            value={props.name}
          />
          <br />
          <br />
          <DialogContentText>Functions</DialogContentText>
          <div className={classes.root}>
            {roleFunctions.map((func) => (
              <Accordion
                expanded={expanded === func.name}
                onChange={handleChangeAccordian(func.name)}
              >
                <AccordionSummary
                  expandIcon={<ExpandMoreIcon />}
                  aria-controls="panel1bh-content"
                  id="panel1bh-header"
                >
                  <Typography className={classes.heading}>
                    {func.name}
                  </Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Typography>{func.desc}</Typography>
                </AccordionDetails>
              </Accordion>
            ))}
          </div>
        </DialogContent>
        <DialogActions></DialogActions>
      </Dialog>
    </>
  );
}

export default RoleBlock;
