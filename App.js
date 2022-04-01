import React, {useEffect} from 'react';
import {SafeAreaView, ToastAndroid, Text, StatusBar, Button} from 'react-native';

import {DeviceEventEmitter} from 'react-native';
import ReactNativeForegroundService from '@supersami/rn-foreground-service';
// importing Service to call
import AppLauncher from 'react-native-app-launcher'
const App = () => {
  // NOTIFICATION PARAMETERS : these are the parameters we have to start a foreground service , * indicate Required.
  // id, * // Id of the notification must be a int
  // title // title of the notification
  // message  // message inside of the notification
  // vibration
  // visibility  ,
  // icon // if you want to use custom icons, you can just use theme by giving their name without extension, make sure they are drawable. for eg = ic_launcher,
  // largeicon // if you want to use custom icons, you can just use theme by giving their name without extension, make sure they are drawable. for eg = ic_launcher,
  // importance // importance, you can read about it on google's official notification docs
  // number // importance, you can read about it on google's official notification docs
  // button // want a button in the notification, set it true
  // buttonText // the text of the button.
  // buttonOnPress // onPress Button, set a string here, onClick your app will reopen with that string in device emitter
  // mainOnPress  // onPress Main Notification, set a string here, onClick your app will reopen with that string in device emitter



  const onStart = () => {
    AppLauncher.setAlarm('myAlarm', 12345,true)
  };

  const onStop = () => {
    // Make always sure to remove the task before stoping the service. and instead of re-adding the task you can always update the task.
    AppLauncher.clearAlarm('myAlarm')
  };
  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView
        style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
        <Text>An Example for React Native Foreground Service. </Text>
        <Button title={'Start'} onPress={onStart}/>
        <Text>An Example for React Native Foreground Service. </Text>
        <Text>An Example for React Native Foreground Service. </Text>
        <Text>An Example for React Native Foreground Service. </Text>
        <Button title={'Stop'} onPress={onStop} />
      </SafeAreaView>
    </>
  );
};

export default App;
