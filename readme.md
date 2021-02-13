# Cordova - Launcher List App

## About
> inspired by other plugins react native and cord

## how to add
```sh
cordova plugins add cordova-launcher-list-app
```
## how to use

### List All Apps

```js
let allApps = []

async function getAllIcons () {
  return new Promise((resolve, reject) => {
    window.launcherList.getApps({
      success: (data) => {
        resolve(JSON.parse(data.result))
      }
    })
  })
}

getAllIcons().then(r => {
  // return all apps
  // [{label: "YouTube", name: "com.google.youtube, icon: 'base64 encoded'}]
  allApps = r
}).catch(err => console.log(err))

```
### Launch a App

```js
async function openApp (appName) {
  return new Promise((resolve, reject) => {
    window.launcherList.launch(
      { packageName: appName },
      data => {
        this.data = data
        resolve(data)
      },
      err => {
        this.data = JSON.stringify(err)
        reject(err)
      }
    )
  })
}

openApp(allApps[0].name).then(e => {
  console.log('launched')
}).catch(err => console.log(err))

```