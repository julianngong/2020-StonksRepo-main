import requests
import websocket                                         #Library needed for websocket interaction

ws = websocket.WebSocket()                                #Creating a websocket
ws.connect("ws://localhost:8080/socket")                  #Connecting to the server's websocket

teamName = "Test Team"
teamCode = "Code"

r = requests.get('http://localhost:8080/add_bot?teamName=Test Team&teamCode=Code&botName=Testy')  #creating the bot


ws.send(teamName + "#" + teamCode)                         #Sending the credentials of the team

r = requests.get('http://localhost:8080/subscribe_to_stonks?teamName=Test Team&teamCode=Code&stonkName=Apple')          #subscribeing to a stonk (Apple)
r = requests.get('http://localhost:8080/unsubscribe_to_stonks?teamName=Test Team&teamCode=Code&stonkName=Apple')        #unsubscribeing to a stonk (Apple)
r = requests.get('http://localhost:8080/subscribe_to_stonks?teamName=Test Team&teamCode=Code&stonkName=Apple')
r = requests.get('http://localhost:8080/subscribe_to_stonks?teamName=Test Team&teamCode=Code&stonkName=Amazon')

while True:
    print("Receiving data: ")
    d = ws.recv()                                           #Receiving the data sent by the server's websocket
    print(d)


ws.close()                                 