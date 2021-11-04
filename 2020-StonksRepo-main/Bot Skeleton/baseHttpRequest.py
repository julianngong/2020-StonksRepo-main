import requests
#'python -m pip install requests' to get


class __Http_Object:
    httpBase = 'https://Address.com'
    errors = []
    hasError = False

    def addError(self, error):
        self.hasError = True
        self.errors.append(error)
    
    def hasErrors(self):
        return self.hasError

    def getErrors(self):
        return self.errors

    def resetErrors(self):
        self.errors = []
        self.hasError = False

    def setHttpBase(self, base):
        self.httpBase = base

    def makeRequest(self, trail):
        if self.hasError:
            return "", False
        response = requests.get(self.httpBase + trail)
        if not response.ok:
            return "", False
        
        return response.json(), True

Handler = __Http_Object()

class Request_Holder:
    def __init__(self, name, *argNames):
        self.name = name
        self.args = []
        for n in argNames:
            self.args.append(n)
    
    def __err__sendTooManyArgumentsError(self, errorVal):
        err = "Error in the http request called " + self.name[1:] + ". "
        err += "Too many arguments (arg " + errorVal + " onwards)."
        Handler.addError(err)

    def withArgs(self, *values):
        request = self.name
        if len(self.args) == 0:
            return request, True
        request += '?'
        i = 0
        for val in values:
            if i > 0:
                request += '&'
            if i >= len(self.args):
                self.__err__sendTooManyArgumentsError(val)
                return "", False
            request += self.args[i] + '=' + val
            i += 1

        return request, True

Requests = {
    "is_bot_name_valid" : Request_Holder("/is_bot_name_valid", "botName"),
    "add_bot"           : Request_Holder("/add_bot", "teamName", "teamCode", "botName"),
    "get_stonk_list"    : Request_Holder("/get_stonk_list"),
    "get_stonk_price"   : Request_Holder("/get_stonk_price", "stonkName"),
    "get_stonk_history" : Request_Holder("/get_stonk_history", "stonkName", "historyLength"),
    "get_bot_info"      : Request_Holder("/get_bot_info", "teamName", "teamCode"),
    "buy_stonks"        : Request_Holder("/buy_stonks", "teamName", "teamCode", "stonkName", "stonkAmount"),
    "sell_stonks"       : Request_Holder("/sell_stonks", "teamName", "teamCode", "stonkName", "stonkAmount"),
} #Todo, add covering and shorting to this
