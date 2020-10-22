import React from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import ChatPage from "./pages/ChatPage";
import ChoosePage from "./pages/ChoosePage";
import CreatePage from "./pages/CreatePage";
import HomePage from "./pages/HomePage";
import InteractPage from "./pages/InteractPage";
import Join from "./components/ChatComponents/Join/Join";

const Routes = () => {
  return (
    <Switch>
      <Route path="/home" exact>
        <HomePage />
      </Route>
      <Route path="/build" exact>
        <CreatePage />
      </Route>
      <Route path="/chat" exact>
        <ChatPage />
      </Route>
      <Route path="/joinchat" exact>
        <Join />
      </Route>
      <Route path="/join" exact>
        <InteractPage />
      </Route>
      <Route path="/choose" exact>
        <ChoosePage />
      </Route>
      <Redirect to="/home" />
    </Switch>
  );
};

export default Routes;
