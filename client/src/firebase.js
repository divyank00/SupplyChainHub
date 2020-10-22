import firebase from "firebase/app";
import "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyAUHqovCHG-WdZjCxc2Vdy_dWA0m_XNsF4",
  authDomain: "supplychainhub-5447a.firebaseapp.com",
  databaseURL: "https://supplychainhub-5447a.firebaseio.com",
  projectId: "supplychainhub-5447a",
  storageBucket: "supplychainhub-5447a.appspot.com",
  messagingSenderId: "404528752283",
  appId: "1:404528752283:web:d3ff7f424e85312facc83e",
  measurementId: "G-P9QGM3MT7Q",
};
// Initialize Firebase
firebase.initializeApp(firebaseConfig);

export default firebase;
