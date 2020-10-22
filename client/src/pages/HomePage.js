import { Button, Container, Typography } from "@material-ui/core";
import React from "react";
import { Link } from "react-router-dom";
import ArrowForwardIcon from "@material-ui/icons/ArrowForward";
function HomePage() {
  return (
    <div className="landing-container">
      <br />
      <br />
      <br />
      <br />
      <br />
      <br />
      <Container
        maxWidth="lg"
        style={{ display: "flex", justifyContent: "space-between" }}
      >
        <div style={{ flex: 0.6 }}>
          <Typography className="heading" variant="h3">
            $upply
          </Typography>
          <Typography className="heading" variant="h1">
            Chain Hub
          </Typography>
          <Typography variant="h6" className="sub-heading">
            Create customized Supply Chain for your Products without code
          </Typography>
          <br />
          <Link
            to="/choose"
            style={{ textDecoration: "none", color: "#7e89fd" }}
          >
            <Button
              variant="contained"
              endIcon={<ArrowForwardIcon />}
              style={{
                borderRadius: 20,
                padding: "0.5rem 1.5rem",
              }}
            >
              Get Started
            </Button>
          </Link>
        </div>
        <div style={{ flex: 0.4 }}>
          <img
            style={{ width: "85%" }}
            src={require("../assests/landing_bg2.svg")}
          />
        </div>
      </Container>
      <br />
      <br />
      <br />
      <br />
      <br />
      <br />
      <br />
    </div>
  );
}

export default HomePage;
