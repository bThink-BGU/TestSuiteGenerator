var event1, event2
var eventList = ['send', 'ackOk', 'ackNOk', 'recAck', 'recNak', 'r2tLoss', 't2rLoss', 'r2tReordered',
    't2rReordered', 'success', 'dup_error', 'lostError']
bthread('GenerateKohnCTD', function () {
    for ( event1 of eventList) {
        for (event2 of eventList ){

            f = function(eventA, eventB) {
                bthread('k' + eventA + eventB, function () {

                    sync({waitFor: bp.Event(eventA)})
                    sync({waitFor: bp.Event(eventB)})
                    bp.log.info("--------------2 event1 and event2" + eventA + " " + eventB);
                    var goal = 'Goal' + eventA + eventB
                    // sync({request: bp.Event(goal), block: bp.Event(goal).negate()})
                    sync({request: bp.Event(goal)})
                })
            }
            f(event1, event2)
        }
    }
})

