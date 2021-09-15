
ctx.bthread('Send', 't_send', function (entity) {
  while (true) {
    sync({request: Event('send')})
  }
})

ctx.bthread('AckOk', 't_ackOk', function (entity) {
  while (true) {
    sync({request: Event('ackOk')})
  }
})

ctx.bthread('AckNok', 't_ackNok', function (entity) {
  while (true) {
    sync({request: Event('ackNok')})
  }
})

ctx.bthread('RecAck', 'r_recAck', function (entity) {
  while (true) {
    sync({request: Event('recAck')})
  }
})

ctx.bthread('RecNak', 'r_recNak', function (entity) {
  while (true) {
    sync({request: Event('recNak')})
  }
})

ctx.bthread('R2tLoss', 'r2t_loss', function (entity) {
  while (true) {
    sync({request: Event('r2tLoss')})
  }
})

ctx.bthread('T2rLoss', 't2r_loss', function (entity) {
  while (true) {
    sync({request: Event('t2rLoss')})
  }
})

ctx.bthread('R2tReorder', 'r2t_reorder', function (entity) {
  while (true) {
    sync({request: Event('r2tReorder')})
  }
})

ctx.bthread('T2rReorder', 't2r_reorder', function (entity) {
  while (true) {
    sync({request: Event('t2rReorder')})
  }
})

ctx.bthread('T_success', 'T_SUCCESS', function (entity) {
  sync({request: Event('success')})
  bp.log.info("Effect for success, e={0}", entity);

//  if (use_accepting_states) {
//    // AcceptingState.Continuing()
//    AcceptingState.Stopping()
//  }
   sync({block: bp.all})
})
ctx.bthread('T_dup_error', 'T_DUP_ERROR', function (entity) {
  sync({request: Event('dup_error')})
//  if (use_accepting_states) {
//    // AcceptingState.Continuing()
//    AcceptingState.Stopping()
//  }
   sync({block: bp.all})
})
ctx.bthread('T_lost_error', 'T_LOST_ERROR', function (entity) {
  sync({request: Event('lostError')})
//  if (use_accepting_states) {
//    // AcceptingState.Continuing()
//    AcceptingState.Stopping()
//  }
   sync({block: bp.all})
})

//----------------------------------------
ctx.populateContext([
  ctx.Entity("abpData", "abp", {
    t_seq: 0,
    r_seq: 0,
    t2r: [],
    r2t: [],
    send_next: 0,
    received: [],
    // TO_BE_SEND: [],
    TO_BE_SEND: ['A', 'B', 'C', 'D', 'E', 'V'],
    SEQ_MAX: 2,
    CHN_SIZE: 2,
    CHN_LOSS: true,
    CHN_REORDERED: true
  })
  // ctx.Entity("abpData", "abp", {t_seq:0, r_seq:0, t2r:[], r2t:[], send_next:0, received:[], TO_BE_SEND:['a', 'b', 'c'], SEQ_MAX:2, CHN_SIZE:2, CHN_LOSS:false, CHN_REORDERED:false})
])

ctx.registerQuery('t_send', function (entity) {
  return entity.t2r.length < entity.CHN_SIZE && entity.send_next < entity.TO_BE_SEND.length
})
ctx.registerQuery('t_ackOk', function (entity) {
  return entity.r2t.length > 0 && entity.r2t[0] == (entity.t_seq + 1) % entity.SEQ_MAX
})
ctx.registerQuery('t_ackNok', function (entity) {
  return entity.r2t.length > 0 && entity.r2t[0] != (entity.t_seq + 1) % entity.SEQ_MAX
})
ctx.registerQuery('r_recAck', function (entity) {
  return entity.t2r.length > 0 && entity.t2r[0][0] == entity.r_seq && entity.r2t.length < entity.CHN_SIZE
})
ctx.registerQuery('r_recNak', function (entity) {
  return entity.t2r.length > 0 && entity.t2r[0][0] != entity.r_seq && entity.r2t.length < entity.CHN_SIZE
})
ctx.registerQuery('t2r_loss', function (entity) {
  return entity.t2r.length > 0 && entity.CHN_LOSS
})
ctx.registerQuery('r2t_loss', function (entity) {
  return entity.r2t.length > 0 && entity.CHN_LOSS
})
ctx.registerQuery('t2r_reorder', function (entity) {
  return entity.t2r.length > 0 && entity.CHN_REORDERED
})
ctx.registerQuery('r2t_reorder', function (entity) {
  return entity.r2t.length > 0 && entity.CHN_REORDERED
})

ctx.registerQuery('T_SUCCESS', function (entity) {
  return entity.received.toString() == entity.TO_BE_SEND.toString()
})
ctx.registerQuery('T_DUP_ERROR', function (entity) {
  return entity.received.filter(x => x == 'a').length > 1 || entity.received.filter(x => x == 'b').length > 1 || entity.received.filter(x => x == 'c').length > 1
})
ctx.registerQuery('T_LOST_ERROR', function (entity) {
  return (entity.received.includes(String('b')) || entity.received.includes(String('c'))) && !entity.received.includes(String('a'))
})


ctx.registerEffect('send', function () {
  e = ctx.getEntityById('abpData')
  var t = [e.t_seq, e.TO_BE_SEND[e.send_next]]
  e.t2r.push(t)
  // bp.log.info("Effect for send, e={0}", e);
  ctx.updateEntity(e)
})

ctx.registerEffect('ackOk', function (e) {
  e = ctx.getEntityById('abpData')
  e.r2t.shift()
  e.t_seq = (e.t_seq + 1) % e.SEQ_MAX
  e.send_next += 1
  // bp.log.info("Effect for ackOk, e={0}", e);
  ctx.updateEntity(e)
})

ctx.registerEffect('ackNok', function (e) {
  e = ctx.getEntityById('abpData')
  e.r2t.shift()
  // bp.log.info("Effect for ackNok, e={0}", e);
  ctx.updateEntity(e)
})


ctx.registerEffect('recAck', function (e) {
  e = ctx.getEntityById('abpData')
  x = e.t2r.shift()
  payload = x[1]
  e.r_seq = (e.r_seq + 1) % e.SEQ_MAX
  e.r2t.push(e.r_seq)
  e.received.push(payload)
  // bp.log.info("Effect for recAck, e={0}", e);
  ctx.updateEntity(e)
})

ctx.registerEffect('recNak', function (e) {
  e = ctx.getEntityById('abpData')
  e.t2r.shift()
  e.r2t.push(e.r_seq)
  // bp.log.info("Effect for recNak, e={0}", e);
  ctx.updateEntity(e)
})

ctx.registerEffect('t2rLoss', function (e) {
  e = ctx.getEntityById('abpData')
  e.t2r.shift()
  // bp.log.info("Effect for t2rLoss, e={0}", e);
  ctx.updateEntity(e)
})
ctx.registerEffect('r2tLoss', function (e) {
  e = ctx.getEntityById('abpData')
  e.r2t.shift()
  // bp.log.info("Effect for r2tLoss, e={0}", e);
  ctx.updateEntity(e)
})
ctx.registerEffect('t2rReorder', function (e) {
  e = ctx.getEntityById('abpData')
  e.t2r.reverse()
  // bp.log.info("Effect for t2rReorder, e={0}", e);
  ctx.updateEntity(e)
})
ctx.registerEffect('r2tReorder', function (e) {
  e = ctx.getEntityById('abpData')
  e.r2t.reverse()
  // bp.log.info("Effect for r2tReorder, e={0}", e);
  ctx.updateEntity(e)
})

ctx.registerEffect('dataToBeSend', function (eventData) {
  e = ctx.getEntityById('abpData')
  e.TO_BE_SEND.push(eventData.info)
  // bp.log.info("Effect for dataToBeSend, ee.data={0}", e);
  ctx.updateEntity(e)
})
ctx.registerEffect('doT2rLost', function () {
  e = ctx.getEntityById('abpData')
  // bp.log.info("Effect for doT2rLost, e.data={0}", e);
  ctx.updateEntity(e)
})
ctx.registerEffect('doR2tLost', function () {
  e = ctx.getEntityById('abpData')
  // bp.log.info("Effect for doR2tLost, e.data={0}", e.data);
  ctx.updateEntity(e)
})
ctx.registerEffect('doT2rReorder', function () {
  e = ctx.getEntityById('abpData')
  // bp.log.info("Effect for doT2rReorder, e.data={0}", e.data);
  ctx.updateEntity(e)
})
ctx.registerEffect('doR2tReorder', function () {
  e = ctx.getEntityById('abpData')
  // bp.log.info("Effect for doR2tReorder, e.data={0}", e);
  ctx.updateEntity(e)
})


//-------------------------------------------------------
var event1, event2
var eventList = ['send', 'ackOk', 'ackNok', 'recAck', 'recNak', 'r2tLoss', 't2rLoss', 'r2tReorder',
  't2rReorder', 'success', 'dup_error', 'lostError']
bthread('GenerateKohnCTD', function () {
  for ( event1 of eventList) {
    for (event2 of eventList ){

      f = function(eventA, eventB) {
        bthread('k' + eventA + eventB, function () {

          sync({waitFor: bp.Event(eventA)})
          sync({waitFor: bp.Event(eventB)})
          var goal = 'Goal' + eventA + eventB
          // sync({request: bp.Event(goal), block: bp.Event(goal).negate()})
          sync({request: bp.Event(goal)})
        })
      }
      f(event1, event2)
    }
  }
})

