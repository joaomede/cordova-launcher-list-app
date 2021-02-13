module.exports = {
  getApps: function (args) {
    cordova.exec(
      (!args.success) ? null : args.success,
      function (error) { alert('Installed Apps getApps Error:' + error) },
      'LauncherList',
      'getApps',
      []
    )
  },
  launch: function (args) {
    cordova.exec(
      (!args.success) ? null : args.success,
      function (error) { alert('Installed Apps getApps Error:' + error) },
      'LauncherList',
      'launch',
      [args]
    )
  }
}
