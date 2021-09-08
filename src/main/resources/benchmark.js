// Number of events
if (typeof n == 'undefined') n = 5

// Length of the 'good' scenario
if (typeof l == 'undefined') l = 5

// Number of events in the anti-scenario
if (typeof k == 'undefined') k = 2

// Length of the test
if (typeof t == 'undefined') t = 20


const GOAL = bp.Event('*')

let events = []
for (let c = 65; c < 65 + n; c++) {
    let e = String.fromCharCode(c)
    eval("const " + e + " = bp.Event('" + e + "')")
    eval("events.push(" + e + ")")
}

bp.registerBThread("GoalMarker",
    function () {
        for (let i = 0; i < l; i++) {
            bp.sync({waitFor: A}, {wish: A})
        }

        bp.sync({request: GOAL})
    })

bp.registerBThread(function () {
    for (let i = 0; i < t; i++) {
        bp.sync({request: events});
    }
})

// No k repetitions of the same letter
events.forEach(e => {
    bp.registerBThread(function () {
        loop: while (true) {
            for (let i = 0; i < k; i++) {
                if (!bp.sync({waitFor: events}, i == k - 1 ? {object: e} : null).equals(e)) {
                    continue loop;
                }
            }

            bp.sync({block: GOAL});
        }
    })
})

