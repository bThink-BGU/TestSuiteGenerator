// bthread('StartSimulator', function (entity) {
//   // sync({request: bp.Event('startSimulator'), block: [bp.all]})
//   sync({request: bp.Event('startSimulator')})
// })
bthread('DataToBeSend', function (entity) {
    sync({request: bp.Event('dataToBeSend', {info:"A"}), block: bp.Event("success")})
    sync({request: bp.Event('dataToBeSend', {info:"B"})})
    sync({request: bp.Event('dataToBeSend', {info:"C"})})
    sync({request: bp.Event('dataToBeSend', {info:"D"})})
    sync({request: bp.Event('dataToBeSend', {info:"E"})})
    sync({request: bp.Event('dataToBeSend', {info:"V"})})
})
// bthread('DoT2rLost', function (entity) {
//   while (true) {
//     sync({request: bp.Event('doT2rLost')}, 10000)
//   }
// })
// bthread('DoR2rLost', function (entity) {
//   while (true) {
//     sync({request: bp.Event('doR2tLost')}, 10000)
//   }
// })
// bthread('DoT2rReorder', function (entity) {
//   while (true) {
//     sync({request: bp.Event('doT2rReorder')}, 10000)
//   }
// })
// bthread('DoR2tReorder', function (entity) {
//   while (true) {
//     sync({request: bp.Event('doR2tReorder')}, 10000)
//   }
// })
