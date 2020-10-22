import { makeStyles, Typography } from "@material-ui/core";
import { motion } from "framer-motion";
import React, { useState } from "react";
import HorizontalLabelPositionBelowStepper from "../components/Stepper";

const useStyles = makeStyles((theme) => ({
  sideDiv: {
    width: "30rem",
    height: "93vh",
    backgroundColor: theme.palette.primary.main,
    boxShadow: "10px 10px 50px rgba(0,0,0,0.16)",
    zIndex: "5",
    display: "flex",
    padding: "5rem",
    color: "white",
    flexDirection: "column",
  },
}));

function CreatePage() {
  const classes = useStyles();
  const [activeStep, setActiveStep] = useState(0);

  const SideBar = () => {
    switch (activeStep) {
      case 0:
        return (
          <motion.div
            className={classes.sideDiv}
            initial={{ x: -250 }}
            animate={{ x: 0 }}
            transition={{ duration: 0.5, type: "tween" }}
          >
            <Typography variant="h3">Step {activeStep + 1}</Typography>
          </motion.div>
        );
      case 1:
        return (
          <motion.div
            className={classes.sideDiv}
            initial={{ x: -250 }}
            animate={{ x: 0 }}
            transition={{ duration: 0.5, type: "tween" }}
          >
            <Typography variant="h3">Step {activeStep + 1}</Typography>
          </motion.div>
        );
      case 2:
        return (
          <motion.div
            className={classes.sideDiv}
            initial={{ x: -250 }}
            animate={{ x: 0 }}
            transition={{ duration: 0.5, type: "tween" }}
          >
            <Typography variant="h3">Step {activeStep + 1}</Typography>
          </motion.div>
        );
      case 3:
        return (
          <motion.div
            className={classes.sideDiv}
            initial={{ x: -250 }}
            animate={{ x: 0 }}
            transition={{ duration: 0.5, type: "tween" }}
          >
            <Typography variant="h3">Step {activeStep + 1}</Typography>
          </motion.div>
        );

      default:
        return <></>;
    }
  };

  return (
    <div style={{ display: "flex" }}>
      <SideBar />
      <HorizontalLabelPositionBelowStepper
        activeStep={activeStep}
        setActiveStep={setActiveStep}
        style={{ flex: 0.8 }}
      />
    </div>
  );
}

export default CreatePage;
