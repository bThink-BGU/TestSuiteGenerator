
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

ctx.bthread('R2tReorder', 'r2t_reordered', function (entity) {
  while (true) {
    sync({request: Event('r2tReorder')})
  }
})

ctx.bthread('T2rReorder', 't2r_reordered', function (entity) {
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

