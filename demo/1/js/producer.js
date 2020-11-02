const colors = require('colors');
const Socket = require('simple-websocket')


const userArguments = process.argv.slice(2);
const companyIdentifier = userArguments[0]
const documentIdentifier = userArguments[1]
const socket = new Socket('ws://localhost:8080/ws/company/' + companyIdentifier + '/document/' + documentIdentifier)

socket.on('connect', () => {
  console.log("CONNECTED, sending info")
  socket.send(
    JSON.stringify(
        {
            revisionId: 10, tasks: [...Array(300)].map((_, i) => `some string with iterating value ${i}`)
        }
    )
  )
})

socket.on('data', data => {
  console.log('READ'.yellow + ' | ' + data)
})