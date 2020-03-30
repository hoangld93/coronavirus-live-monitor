/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import MainComponent from './MainComponent';

import React, { Component } from 'react';

export default class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
    };
  }

  render() {
    return (
      <MainComponent
        dataListRemote={this.props.dataListRemote}
        isUseMyServer={this.props.isUseMyServer}
      />
    );
  }
}


// const App: () => React$Node = () => {


//   return (
//     <>
//       <MainComponent />
//     </>
//   );
// };