const colors = require('colors');
const Socket = require('simple-websocket')

const userArguments = process.argv.slice(2);
const companyIdentifier = userArguments[0]
const documentIdentifier = userArguments[1]
const socket = new Socket('ws://localhost:8081/ws/company/' + companyIdentifier + '/document/' + documentIdentifier)


socket.on('connect', () => {
  console.log("CONNECTED, but just going to listen.")
})

socket.on('data', data => {
  console.log('READ'.yellow + ' | ' + data)
})