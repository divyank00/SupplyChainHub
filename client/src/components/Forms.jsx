import {
  Button,
  Checkbox,
  Container,
  FormControl,
  FormControlLabel,
  InputLabel,
  ListItem,
  ListItemIcon,
  ListItemText,
  Paper,
  Slide,
  Typography,
} from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import Dialog from "@material-ui/core/Dialog";
import DialogActions from "@material-ui/core/DialogActions";
import DialogContent from "@material-ui/core/DialogContent";
import DialogContentText from "@material-ui/core/DialogContentText";
import DialogTitle from "@material-ui/core/DialogTitle";
import Fab from "@material-ui/core/Fab";
import MenuItem from "@material-ui/core/MenuItem";
import Select from "@material-ui/core/Select";
import { makeStyles } from "@material-ui/core/styles";
import TextField from "@material-ui/core/TextField";
import AddIcon from "@material-ui/icons/Add";
import CheckIcon from "@material-ui/icons/Check";
import CachedIcon from "@material-ui/icons/Cached";

import DeleteIcon from "@material-ui/icons/Delete";
import axios from "axios";
import { motion } from "framer-motion";
import React, { useEffect, useState } from "react";
import firebase from "../firebase";
import RoleBlock from "./RoleBlock";

const useStyles = makeStyles((theme) => ({
  root: {
    "& > *": {
      margin: theme.spacing(1),
      width: "100%",
    },
  },
  formControl: {
    margin: theme.spacing(1),
    minWidth: 120,
  },
  selectEmpty: {
    marginTop: theme.spacing(2),
  },
  extendedIcon: {
    marginRight: theme.spacing(1),
  },
  xScrollable: {
    width: "auto",
    whiteSpace: "noWrap",
    overflowX: "scroll",
    paddingTop: theme.spacing(5),
    paddingBottom: theme.spacing(5),
    textAlign: "center",
  },
  container: {
    minHeight: "55vh",
  },
  deployContainer: {
    display: "flex",
    flex: 1,
    flexDirection: "row",
    justifyContent: "space-evenly",
    alignItems: "center",
    height: "55vh",
    width: "100%",
  },
  codebox: {
    flex: 1,
    height: "100%",
    backgroundColor: "#202225",
    overflowY: "scroll",
    color: "white",
  },

  codeboxHeading: {
    backgroundColor: "#121418",
    padding: theme.spacing(2),
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    color: "white",
  },
  content: {
    paddingLeft: theme.spacing(5),
  },

  detailContainer: {
    padding: theme.spacing(5),
    backgroundColor: theme.palette.primary.main,
    color: "white",
  },
  compiled: {
    backgroundColor: "#4BB543",
    color: "white",
  },
}));

export function YourDetails() {
  const classes = useStyles();
  const [name, setName] = useState("");
  const [company_name, setCompany_name] = useState("");
  const [address, setAddress] = useState("");

  return (
    <Container maxWidth="sm">
      <motion.div
        className={classes.container}
        initial={{ y: 100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, type: "tween" }}
      >
        <Typography variant="h4" component="h2">
          Your Details
        </Typography>
        <br />
        <br />
        <form className={classes.root} noValidate autoComplete="off">
          <TextField
            id="outlined-basic1"
            label="Your Name"
            variant="outlined"
            required
            autoFocus
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              localStorage.setItem("owner_name", e.target.value);
            }}
          />
          <TextField
            id="outlined-basic2"
            label="Organisation Name"
            variant="outlined"
            required
            value={company_name}
            onChange={(e) => {
              setCompany_name(e.target.value);
              localStorage.setItem("company_name", e.target.value);
            }}
          />
          <TextField
            id="outlined-basic3"
            label="Your Address"
            variant="outlined"
            required
            multiline
            value={address}
            onChange={(e) => {
              setAddress(e.target.value);
              localStorage.setItem("owner_address", e.target.value);
            }}
          />
        </form>
      </motion.div>
    </Container>
  );
}

export function ProductDetails() {
  const classes = useStyles();
  const [category, setCategory] = React.useState("");

  const [productName, setProductName] = useState("");
  const [productArgs, setProductArgs] = useState([]);
  const [open, setOpen] = React.useState(false);
  const [propertyName, setPropertyName] = useState("");
  const [propertyType, setPropertyType] = useState("");

  useEffect(() => {
    localStorage.setItem("product_args", JSON.stringify(productArgs));
  }, [productArgs]);

  const handleChange = (event) => {
    setCategory(event.target.value);
    localStorage.setItem("product_category", event.target.value);
  };

  const handleTypeChange = (event) => {
    setPropertyType(event.target.value);
  };
  const addProductArg = () => {
    setProductArgs([
      ...productArgs,
      { name: propertyName, type: propertyType },
    ]);
    setPropertyName("");
    setPropertyType("");
    setOpen(false);
    localStorage.setItem("product", productArgs);
  };

  const deleteProductArg = (item) => {
    const newProductArgs = productArgs.filter(
      (property) => property.name !== item.name
    );
    setProductArgs([...newProductArgs]);
  };

  const handleClickOpen = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
  };

  return (
    <Container maxWidth="sm">
      <motion.div
        className={classes.container}
        initial={{ y: 100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, type: "tween" }}
      >
        <Typography variant="h4" component="h2">
          Product Details
        </Typography>
        <br />
        <br />

        <form className={classes.root} noValidate autoComplete="off">
          <TextField
            onChange={(e) => {
              setProductName(e.target.value);
              localStorage.setItem("product_name", productName);
            }}
            value={productName}
            id="outlined-basic1"
            label="Product Name"
            variant="outlined"
            required
            autoFocus
          />
          <FormControl
            variant="outlined"
            className={classes.formControl}
            required
          >
            <InputLabel id="demo-simple-select-filled-label">
              Category
            </InputLabel>
            <Select value={category} onChange={handleChange}>
              <MenuItem value="">
                <em>None</em>
              </MenuItem>
              <MenuItem value={"Industrial"}>Industrial</MenuItem>
              <MenuItem value={"Medical"}>Medical</MenuItem>
              <MenuItem value={"Other"}>Other</MenuItem>
            </Select>
          </FormControl>

          {productArgs.map((item, index) => (
            <ListItem>
              <ListItemIcon>
                <DeleteIcon
                  color="error"
                  cursor="pointer"
                  onClick={() => deleteProductArg(item)}
                />
              </ListItemIcon>
              <ListItemText primary={item.name} secondary={item.type} />
            </ListItem>
          ))}
        </form>
        <br />
        <div>
          <Fab variant="extended" color="primary" onClick={handleClickOpen}>
            <AddIcon className={classes.extendedIcon} />
            Add Property &nbsp;
          </Fab>
          <Dialog
            open={open}
            onClose={handleClose}
            aria-labelledby="form-dialog-title"
          >
            <DialogTitle id="form-dialog-title">Add Property</DialogTitle>
            <DialogContent>
              <DialogContentText>
                Manufactuers will have to make sure to add theses customized
                property values while packing products.
              </DialogContentText>
              <br />
              <TextField
                value={propertyName}
                onChange={(event) => setPropertyName(event.target.value)}
                label="Property Name"
                type="text"
                margin="dense"
              />
              <br />
              <FormControl>
                <InputLabel id="demo-simple-select-label">Type</InputLabel>
                <Select
                  value={propertyType}
                  labelId="demo-simple-select-label"
                  onChange={handleTypeChange}
                  style={{ minWidth: 120 }}
                >
                  <MenuItem aria-label="None" value="" />
                  <MenuItem value={"String"}>String</MenuItem>
                  <MenuItem value={"Integer"}>Integer</MenuItem>
                  <MenuItem value={"Boolean"}>Boolean</MenuItem>
                  <MenuItem value={"Address(Hex)"}>Address(Hex)</MenuItem>
                </Select>
              </FormControl>
            </DialogContent>
            <DialogActions>
              <Button onClick={handleClose} color="primary">
                Cancel
              </Button>
              <Button onClick={addProductArg} color="primary">
                Add
              </Button>
            </DialogActions>
          </Dialog>
        </div>
      </motion.div>
    </Container>
  );
}

export function SupplyChain() {
  const classes = useStyles();
  const [roles, setRoles] = useState([]);
  const [open, setOpen] = React.useState(false);
  const [roleName, setRoleName] = useState("");

  useEffect(() => {
    localStorage.setItem("roles", JSON.stringify(roles));
  }, [roles]);

  const handleClickOpen = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
  };

  const addRole = () => {
    setRoles([...roles, roleName]);
    setOpen(false);
    setRoleName("");
  };

  const removeRole = (role) => {
    const newRoles = roles.filter((item) => item !== role);
    setRoles(newRoles);
  };

  return (
    <div style={{ padding: 20 }}>
      <motion.div
        initial={{ y: 100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, type: "tween" }}
      >
        <br />
        <div>
          {roles.length ? (
            <RoleBlock key="last" blank handleClickOpen={handleClickOpen} />
          ) : (
            <></>
          )}
        </div>
        <br />
        <br />
        <div className={classes.xScrollable}>
          {roles.length ? (
            roles.map((role, index) =>
              roles.length - 1 === index ? (
                <RoleBlock
                  key={index}
                  name={role}
                  removeRole={removeRole}
                  roles={roles}
                  setRoles={setRoles}
                  lastElement
                />
              ) : (
                <RoleBlock
                  key={index}
                  name={role}
                  removeRole={removeRole}
                  setRoles={setRoles}
                  roles={roles}
                />
              )
            )
          ) : (
            <RoleBlock key="last" blank handleClickOpen={handleClickOpen} />
          )}
          <br />
          <br />
          <br />
          <br />
        </div>
        <Dialog
          open={open}
          onClose={handleClose}
          aria-labelledby="form-dialog-title"
        >
          <DialogTitle id="form-dialog-title">Add Role</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Manufactuers will have to make sure to add theses customized role
              values while packing products.
            </DialogContentText>

            <TextField
              value={roleName}
              onChange={(event) => setRoleName(event.target.value)}
              label="Role Name"
              type="text"
              margin="dense"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClose} color="primary">
              Cancel
            </Button>
            <Button onClick={addRole} color="primary">
              Add
            </Button>
          </DialogActions>
        </Dialog>
      </motion.div>
    </div>
  );
}
const solcjs = require("solc-js");
const version = "v0.4.25-stable-2018.09.13";

export function Deployment(props) {
  const [smartContractCode, setSmartContractCode] = useState("");
  const [contract, setContract] = useState(null);
  const [deployed, setDeployed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [compiling, setCompiling] = useState(false);

  useEffect(() => {
    let data = {
      product_name: localStorage.getItem("product_name"),
      roles: JSON.parse(localStorage.getItem("roles")),
    };
    data.roles = ["Owner", ...data.roles, "Customer"];
    getContract(data);
  }, []);

  const getContract = async (data) => {
    axios
      .post("http://127.0.0.1:5000/createcontracts", {
        name: data.product_name,
        role: data.roles,
        tracking: "lotAndProduct",
        selfDestruct: false,
      })
      .then((res) => {
        setSmartContractCode(res.data);
      });
  };

  const compileCode = async () => {
    setCompiling(true);
    const compiler = await solcjs(version);
    const output = await compiler(smartContractCode);
    setContract(output[0]);
    setCompiling(false);
  };

  const createDoc = async () => {
    const data = {
      abi: JSON.stringify(contract.abi),
    };
    await firebase
      .firestore()
      .collection("Contracts")
      .doc(localStorage.getItem("deployed_contract"))
      .set(data);
  };

  const deployContract = async () => {
    const web3 = window.web3;
    const myContract = new web3.eth.Contract(contract.abi);
    myContract
      .deploy({
        data: contract.binary.bytecodes.bytecode,
        arguments: [
          localStorage.getItem("company_name"),
          localStorage.getItem("product_name"),
          localStorage.getItem("product_category"),
          localStorage.getItem("owner_name"),
          "18.92",
          "73.85",
        ],
      })
      .send(
        {
          from: localStorage.getItem("account_address"),
          gas: 4700000,
        },
        (err, res) => {
          if (err) {
            console.log(err);
          }
          if (res) {
            setLoading(true);
            console.log(res);

            setDeployed(true);
          }
        }
      )
      .on("transactionHash", function (transactionHash) {
        localStorage.setItem("contract_deployemnt_txHash", transactionHash);
      })
      .on("receipt", function (receipt) {
        console.log(receipt.contractAddress);
        localStorage.setItem("deployed_contract", receipt.contractAddress); // contains the new contract address
      })
      .on("confirmation", function (confirmationNumber, receipt) {
        createDoc();
        setLoading(false);
      });
  };

  const classes = useStyles();

  if (loading) {
    return (
      <Container
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          color: "white",
          height: "50vh",
        }}
      >
        <CircularProgress style={{ display: "block" }} />
        <p style={{ display: "block" }}>
          Deploying Contract on Matic Mumbai Testnet
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        </p>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <motion.div
        initial={{ y: 100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, type: "tween" }}
        className={classes.deployContainer}
      >
        <div className={classes.codebox}>
          <div className={classes.codeboxHeading}>
            <Typography>SupplyChain.sol</Typography>
            <div>
              <Button
                className={contract ? classes.compiled : ""}
                color="primary"
                variant="contained"
                onClick={compileCode}
                endIcon={contract ? <CheckIcon /> : <></>}
                startIcon={compiling ? <CachedIcon /> : <></>}
              >
                {contract ? "Compiled" : "Compile"}
              </Button>
              &nbsp;&nbsp;&nbsp;
              <Button
                color="primary"
                variant="contained"
                className={deployed ? classes.compiled : ""}
                disabled={!contract}
                onClick={deployContract}
                endIcon={deployed ? <CheckIcon /> : <></>}
              >
                Deploy
              </Button>
            </div>
          </div>
          <div className={classes.content}>
            <pre>{smartContractCode}</pre>
          </div>
        </div>
      </motion.div>
    </Container>
  );
}
export function Completed() {
  const classes = useStyles();

  useEffect(() => {}, []);

  return (
    <motion.div
      initial={{ y: 100, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.5, type: "tween" }}
    >
      <Container maxWidth="md">
        <div className={classes.container}>
          <br />
          <Paper square className={classes.detailContainer} elevation={5}>
            <Typography variant="h3">Contract Address</Typography>
            <Typography variant="h6">
              {localStorage.getItem("deployed_contract")}
            </Typography>
            <br />
            <Typography variant="h3">Transaction Hash</Typography>
            <Typography variant="h6">
              0x8b38a6d785f3ba2784a322b33e6fcc9932458429aeaff88cbe03ac4625ffedfc
            </Typography>
            <br />
            <Typography variant="h3">Owner</Typography>
            <Typography variant="h6">
              {localStorage.getItem("account_address")}
            </Typography>
          </Paper>
        </div>
      </Container>
    </motion.div>
  );
}

export function SelectBox() {
  const classes = useStyles();
  const [state, setState] = useState({
    trackwithlot: false,
    trackwithproduct: true,
  });
  useEffect(() => {
    localStorage.setItem("track", JSON.stringify(state));
  }, [state]);
  const handleChangeCheckbox = (event) => {
    setState({ ...state, [event.target.name]: event.target.checked });
  };

  return (
    <Container maxWidth="xs">
      <br />
      <br />

      <motion.div
        style={{
          display: "flex",
          justifyContent: "center",
          flexDirection: "column",
        }}
        initial={{ y: 100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, type: "tween" }}
      >
        <Typography variant="h4" component="h2">
          Customization Options
        </Typography>
        <br />
        <br />

        <FormControlLabel
          control={
            <Checkbox
              checked={state.trackwithlot}
              onChange={handleChangeCheckbox}
              name="trackwithlot"
              color="primary"
            />
          }
          label="Track With Lot ID "
        />

        <FormControlLabel
          control={
            <Checkbox
              checked={state.trackwithproduct}
              onChange={handleChangeCheckbox}
              name="trackwithproduct"
              color="primary"
            />
          }
          label="Track With Product ID "
        />
      </motion.div>
    </Container>
  );
}
