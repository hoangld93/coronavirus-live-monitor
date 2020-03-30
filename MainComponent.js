import React, { Component } from 'react';
import { StyleSheet, Text, View, SafeAreaView, TouchableWithoutFeedback, AppState, Image } from 'react-native';
import MapView from 'react-native-maps';
import { Marker, PROVIDER_GOOGLE } from 'react-native-maps';
import BottomSheet from 'reanimated-bottom-sheet'
import { FlatList } from 'react-native-gesture-handler'
import BackgroundTimer from 'react-native-background-timer';
import { MAP_STYLE } from './Constant';
import PureChart from 'react-native-pure-chart';
import { TabView, SceneMap } from 'react-native-tab-view';


import localData from './data.json';

const dataUrl = "https://services1.arcgis.com/0MSEUqKaxRlEPj5g/arcgis/rest/services/ncov_cases/FeatureServer/1/query?f=json&where=1%3D1&returnGeometry=false&spatialRel=esriSpatialRelIntersects&outFields=*&orderByFields=Confirmed%20desc%2CCountry_Region%20asc%2CProvince_State%20asc&resultOffset=0&resultRecordCount=250&cacheHint=true";

// const dataUrl = require('./data.json');

// const localData = require('./data.json');

export default class MainComponent extends Component {

  constructor(props) {
    super(props)

    this.state = {
      region: {
        latitude: 30.9756403482891,
        longitude: 112.270692167452,
        latitudeDelta: 8,
        longitudeDelta: 8,
      },
      latlng: {
        latitude: 30.9756403482891,
        longitude: 112.270692167452,
      },

      dataSource: [],

      itemSelectedPos: -1,
      totalConfirmed: 0,
      totalRecovered: 0,
      totalDeaths: 0,

      appState: AppState.currentState,

      isShowAlert: false,
      isMapReady: false,
    }
  }

  getCityTitle(attributes) {
    if (attributes.Province_State)
      return attributes.Province_State
    return attributes.Country_Region
  }

  getStyleItemList = (index) => {
    if (this.state.itemSelectedPos == index) {
      return styles.bottom_sheet_container_header_selected
    }
    return styles.bottom_sheet_container_item
  }

  getFlagFromCountryCode = (countryRegion) => {
    switch (countryRegion) {
      case 'Mainland China':
        return require('./assets/flags/cn.png')

      case 'Hong Kong':
        return require('./assets/flags/hk.png')

      case 'Malaysia':
        return require('./assets/flags/my.png')

      case 'Taiwan':
        return require('./assets/flags/tw.png')

      case 'Thailand':
        return require('./assets/flags/th.png')

      case 'Iran':
        return require('./assets/flags/ir.png')

      case 'Germany':
        return require('./assets/flags/de.png')

      case 'Vietnam':
        return require('./assets/flags/vn.png')

      case 'Japan':
        return require('./assets/flags/jp.png')

      case 'South Korea':
        return require('./assets/flags/kr.png')

      case 'United Arab Emirates':
        return require('./assets/flags/ua.png')

      case 'Singapore':
        return require('./assets/flags/sg.png')

      case 'Italy':
        return require('./assets/flags/it.png')

      case 'France':
        return require('./assets/flags/fr.png')

      case 'Macau':
        return require('./assets/flags/mo.png')

      case 'UK':
        return require('./assets/flags/uk.png')

      case 'British Columbia':
        return require('./assets/flags/bc.png')

      case 'India':
        return require('./assets/flags/in.png')

      case 'Australia':
        return require('./assets/flags/au.png')

      case 'US':
        return require('./assets/flags/us.png')

      case 'Sweden':
        return require('./assets/flags/se.png')

      case 'Canada':
        return require('./assets/flags/ca.png')

      case 'Belgium':
        return require('./assets/flags/be.png')

      case 'Cambodia':
        return require('./assets/flags/kh.png')

      case 'Russia':
        return require('./assets/flags/ru.png')

      case 'Spain':
        return require('./assets/flags/es.png')

      case 'Egypt':
        return require('./assets/flags/eg.png')

      case 'Finland':
        return require('./assets/flags/fi.png')

      case 'Nepal':
        return require('./assets/flags/np.png')

      case 'Sri Lanka':
        return require('./assets/flags/lk.png')

      case 'Lebanon':
        return require('./assets/flags/lb.png')

      case 'Israel':
        return require('./assets/flags/il.png')

      case 'Philippines':
        return require('./assets/flags/ph.png')

      case 'Iraq':
        return require('./assets/flags/iq.png')

      case 'Kuwait':
        return require('./assets/flags/kw.png')

      case 'Afghanistan':
        return require('./assets/flags/af.png')

      case 'Bahrain':
        return require('./assets/flags/bh.png')

      case 'Oman':
        return require('./assets/flags/om.png')

      default:
        return require('./assets/flags/flag_unknown.png')
    }
  }

  getSpaceItemEnd = (index) => {
    if (index == this.state.dataSource.length - 1)
      return <View style={{ height: 100 }} />
  }

  // getChart = () => {
  //   let sampleData = [30, 200, 170, 250, 10]
  //   return <PureChart width={'100%'}
  //     height={500}
  //     backgroundColor='black'
  //     data={sampleData} type='line' />
  // }

  renderItemList = (attributes, index) => {
    return <TouchableWithoutFeedback onPress={() => this.onItemClick(attributes, index)}>
      <SafeAreaView>
        <View style={this.getStyleItemList(index)}>
          <View style={{ flexDirection: 'row', flex: 1.5 }}>
            <Image
              style={{ width: 30, height: 20, marginLeft: 10 }}
              source={
                this.getFlagFromCountryCode(attributes.Country_Region)
              } />
            <Text style={styles.bottom_sheet_title}>{this.getCityTitle(attributes)}</Text>
          </View>
          <Text style={styles.bottom_sheet_title_confirmed}>{attributes.Confirmed}</Text>
          <Text style={styles.bottom_sheet_title_recovered}>{attributes.Recovered}</Text>
          <Text style={styles.bottom_sheet_title_death}>{attributes.Deaths}</Text>
        </View>
        <View style={{ width: '100%', height: 0.5, backgroundColor: 'red' }} />
        {this.getSpaceItemEnd(index)}
      </SafeAreaView>
    </TouchableWithoutFeedback>
  }

  moveCameraTo = (attributes) => {
    if (attributes && this.mapView) {
      this.mapView.animateToRegion({
        latitude: attributes.Lat,
        longitude: attributes.Long_,
        latitudeDelta: 8,
        longitudeDelta: 8,
      }, 1000);
    }
  }

  onItemClick = (attributes, index) => {
    if (index == this.state.itemSelectedPos) return
    this.moveCameraTo(attributes)
    this.setState({
      itemSelectedPos: index
    })
  }

  renderHeader = () => (
    <SafeAreaView style={styles.bottom_sheet_container}>
      <View style={styles.bottom_sheet_container_header}>
        <View style={{ flex: 1.5, alignContent: 'center', alignSelf: 'center' }}>
          <Text style={styles.header_title_country}>Country/Region</Text>
        </View>

        <View style={{ alignItems: 'center', flex: 1 }}>
          <Image
            style={styles.ic_header_confirm}
            source={require('./assets/ic_confirmed.png')} />
          <Text style={styles.bottom_sheet_title_confirmed}>{this.state.totalConfirmed}</Text>
        </View>
        <View style={{ alignItems: 'center', flex: 1 }}>
          <Image
            style={styles.ic_header_confirm}
            source={require('./assets/ic_recovered.png')} />
          <Text style={styles.bottom_sheet_title_recovered}>{this.state.totalRecovered}</Text>

        </View>
        <View style={{ alignItems: 'center', flex: 1 }}>
          <Image
            style={styles.ic_header_confirm}
            source={require('./assets/ic_rip.png')} />
          <Text style={styles.bottom_sheet_title_death}>{this.state.totalDeaths}</Text>
        </View>

      </View>
      <View style={{ height: 250, flex: 1 }}>
        <FlatList
          data={this.state.dataSource}
          disableVirtualization={true}
          keyExtractor={item => item.id}
          renderItem={({ item, index }) => (
            this.renderItemList(item.attributes, index)
          )}
        />
      </View>
    </SafeAreaView>
  )

  onMapLayout = () => {
    this.setState({ isMapReady: true });
  }

  render() {
    return (
      <SafeAreaView style={styles.container}>
        <BottomSheet
          ref={this.bs}
          snapPoints={[100, 300]}
          renderHeader={this.renderHeader}
          initialSnap={1}
          enabledContentGestureInteraction={false}
        />

        <MapView style={styles.map}
          povider={PROVIDER_GOOGLE}
          initialRegion={this.state.region}
          ref={ref => (this.mapView = ref)}
          customMapStyle={MAP_STYLE}
          onLayout={this.onMapLayout}
        >
          {this.state.isMapReady && this.state.dataSource.map((feature) => (
            <Marker
              coordinate={{
                latitude: feature.attributes.Lat,
                longitude: feature.attributes.Long_,
                latitudeDelta: 0.0922,
                longitudeDelta: 0.0421,
              }}
              image={require('./assets/ic_map_marker.png')}
              title={this.getCityTitle(feature.attributes)}
            />
          ))}
        </MapView>
      </SafeAreaView>

      // <View style={styles.container}>
      // <Text style={styles.title}>Hello</Text>
      // </View>
    );
  }

  getData() {
    console.log("IsMyServer", this.props.isUseMyServer)
    if (this.props.isUseMyServer) {
      this.updateDataUI(JSON.parse(this.props.dataListRemote))
    } else {
      return fetch(dataUrl)
        .then((response) => response.json())
        .then((responseJson) => {
          if (responseJson && responseJson.features && responseJson.features[0]) {
            this.updateDataUI(responseJson)
          } else {
            // console.log("fetch empty", this.props.dataListRemote)
            this.updateDataUI(JSON.parse(this.props.dataListRemote))
          }
        })
        .catch((error) => {
          this.updateDataUI(JSON.parse(this.props.dataListRemote))
        });
    }
  }

  updateDataUI = (responseJson) => {
    // console.log("updateDataUI", responseJson)
    // console.log("responseJson ", responseJson.features[0])
    if (responseJson && responseJson.features && responseJson.features[0]) {
      if (this.mapView) {
        // this.moveCameraTo(responseJson.features[0].attributes)

        var _totalConfirmed = 0
        var _totalRecovered = 0
        var _totalDeaths = 0

        responseJson.features.map((feature) => {
          _totalConfirmed += feature.attributes.Confirmed
          _totalRecovered += feature.attributes.Recovered
          _totalDeaths += feature.attributes.Deaths
        })

        this.setState({
          totalConfirmed: _totalConfirmed,
          totalRecovered: _totalRecovered,
          totalDeaths: _totalDeaths,
          dataSource: responseJson.features
        })
      }
    }
  }

  componentDidMount() {
    AppState.addEventListener('change', this._handleAppStateChange);
    this.getData()
  }

  componentWillUnmount() {
    AppState.removeEventListener('change', this._handleAppStateChange);
  }

  _handleAppStateChange = (nextAppState) => {
    if (this.state.appState.match(/inactive|background/) && nextAppState === 'active') {
      console.log('App has come to the foreground!');
    }
    this.getData()
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5FCFF',
  },
  map: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0
  },

  bottom_sheet_container: {
    width: '100%',
    height: 400,
    backgroundColor: 'black'
  },

  bottom_sheet_container_header: {
    flexDirection: 'row',
    backgroundColor: 'black',
    paddingTop: 5,
    paddingBottom: 5,
    height: 50
  },

  bottom_sheet_container_item: {
    flexDirection: 'row',
    backgroundColor: 'black',
    paddingTop: 10,
    paddingBottom: 10
  },

  header_title_country: {
    fontSize: 12,
    fontWeight: 'bold',
    textAlign: 'center',
    color: 'white'
  },

  bottom_sheet_container_header_selected: {
    flexDirection: 'row',
    paddingTop: 10,
    paddingBottom: 10,
    backgroundColor: 'rgba(255, 0, 0, 0.3)'
  },

  bottom_sheet_title: {
    flex: 1.5,
    fontSize: 12,
    fontWeight: 'bold',
    textAlign: 'center',
    color: 'white'
  },

  ic_header_confirm: {
    width: 24,
    height: 24
  },

  bottom_sheet_title_confirmed: {
    flex: 1,
    fontSize: 12,
    fontWeight: 'bold',
    textAlign: 'center',
    color: 'yellow'
  },

  bottom_sheet_title_recovered: {
    flex: 1,
    fontSize: 12,
    fontWeight: 'bold',
    color: 'green',
    textAlign: 'center',
  },

  bottom_sheet_title_death: {
    flex: 1,
    fontSize: 12,
    fontWeight: 'bold',
    color: 'red',
    textAlign: 'center',
  },

  bottom_sheet_content_mess: {
    fontSize: 30,
    fontWeight: 'bold',
    textAlign: 'center',
  }

});