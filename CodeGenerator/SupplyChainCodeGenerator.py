def writeToFile(match_string, insert_string):
    global contents
    if match_string in contents[-1]:  # Handle last line to prevent IndexError
        contents.append(insert_string)
    else:
        for index, line in enumerate(contents):
            if match_string in line and insert_string not in contents[index + 1]:
                contents.insert(index + 1, insert_string)
                break


def addEvent(role):
    global contents
    match_string = "//*addEvents"
    insert_string = """
        event """ + role + """Added(address indexed account);
        event """ + role + """Removed(address indexed account);"""
    writeToFile(match_string, insert_string)

def addModifier(role, info):
    global contents
    match_string = "//*addModifiers"
    insert_string = """
        modifier only""" + role + """{
            
            require(users[msg.sender].role == """ + str(info["role"].index(role) + 1) + """);
            _;
        }"""
    writeToFile(match_string, insert_string)

def getOnlyRole(role, info):
    if info["role"].index(role) == 0:
        return "ContractOwner"
    else:
        return info["role"][info["role"].index(role) - 1]

def addRole(role, info):
    global contents
    match_string = "//*addRoles"

    insert_string = """
        function add""" + role + """(string _name, string _officeAddress, address account) public only""" + getOnlyRole(role, info) +"""{
            
            require(!checkIsUser(account));
            user memory """ + role.lower() + """ = user({
                role: 1,
                userId : account,
                parentId : msg.sender,
                currentQuantity : 0,
                name: _name,
                officeAddress: _officeAddress
            });
            
            users[account] = """ + role.lower() + """;
            users[msg.sender].childIds[account] = true;
            setUser(account);
            emit """ + role + """Added(account);
        }"""
    writeToFile(match_string, insert_string)


def addForSale(role, info):
    global contents
    if info["role"].index(role) < (len(info["role"]) -1):
        match_string = "//*addForSale"
        insert_string = """
            function forSaleLotBy""" + role + """(string[] memory _lotId, uint _price) public only""" + role + """ packed(_lotId){
                
                for(uint i = 0;i<_lotId.length;i++){
                    lots[_lotId[i]].sellingPrices[msg.sender] = _price;
                    lots[_lotId[i]].productState = State.ForSale;
                }
                users[msg.sender].currentQuantity+=_lotId.length;
                emit ForSale();
            }"""
        writeToFile(match_string, insert_string)
    else:
        return


def addPayFrom(role, info):
    global contents
    if info["role"].index(role) < (len(info["role"]) -1):
        match_string = "//*addPayFrom"
        insert_string = """
            function payFrom""" + info["role"][info["role"].index(role) + 1] + """To""" + role + """(uint _quantity, uint _totalBuyingPrice, string _txnHash) public only""" + info["role"][info["role"].index(role) + 1] + """{
                
                require(_quantity<=users[users[msg.sender].parentId].currentQuantity);
                users[msg.sender].currentQuantity+=_quantity;
                users[users[msg.sender].parentId].currentQuantity-=_quantity;
                deal memory dealDetails = deal({
                    txnHash: _txnHash,
                    capacity: _quantity,
                    buyerAddress: msg.sender,
                    buyingPrice:  _totalBuyingPrice,
                    sellerAddress: users[msg.sender].parentId,
                    sellingPrice: 0
                });
                deals[_txnHash] = dealDetails;
                emit PaymentSuccessful();
            }"""
        writeToFile(match_string, insert_string)
    else:
        return

def addSellTo(role, info):
    global contents
    if info["role"].index(role) < (len(info["role"]) -1):
        match_string = "//*addSellTo"
        insert_string = """
            function sellLotTo""" + info["role"][info["role"].index(role) + 1] + """(string _txnHash, uint _totalSellingPrice, string[] memory _lotId) public only""" + role + """ forSale(_lotId){
                
                deal storage activeDeal = deals[_txnHash];
                if(activeDeal.buyingPrice!=_totalSellingPrice){
                    activeDeal.buyerAddress.transfer(activeDeal.buyingPrice);
                    emit DealFailed(activeDeal.buyerAddress);
                }else{
                    activeDeal.sellingPrice = _totalSellingPrice;
                    deals[_txnHash] = activeDeal;
                    for(uint i = 0;i<_lotId.length;i++){
                        lots[_lotId[i]].buyingPrices[activeDeal.buyerAddress] = (activeDeal.buyingPrice/activeDeal.capacity);
                        lots[_lotId[i]].sellingPrices[msg.sender] = (activeDeal.sellingPrice/activeDeal.capacity);
                        lots[_lotId[i]].buyingPrices[msg.sender] = 0;
                        lots[_lotId[i]].currentOwner = activeDeal.buyerAddress;
                        lots[_lotId[i]].productState = State.Sold;
                    }
                    emit Sold();
                }
            }"""
        writeToFile(match_string, insert_string)
    else:
        return


def addShipLot(role, info):
    global contents
    if info["role"].index(role) < (len(info["role"]) -1):
        match_string = "//*addShipLot"
        insert_string = """
            function shipLotFrom""" + role + """To""" + info["role"][info["role"].index(role) + 1] + """(string[] memory _lotId, string _txnHash) public only""" + role + """ validDeal(_txnHash) sold(_lotId){
                
                require(msg.sender!=deals[_txnHash].buyerAddress);
                for(uint i = 0;i<_lotId.length;i++){
                    lots[_lotId[i]].trackUser[2] = deals[_txnHash].buyerAddress;
                    lots[_lotId[i]].trackTxn[2] = _txnHash;
                    lots[_lotId[i]].productState = State.Shipped;
                }
                emit Shipped();
            }"""
        writeToFile(match_string, insert_string)
    else:
        return


def addReceivedBy(role, info):
    global contents
    if info["role"].index(role) < (len(info["role"]) -1):
        match_string = "//*addReceivedBy"
        insert_string = """
            function receivedBy""" + info["role"][info["role"].index(role) + 1] + """(string[] memory _lotId) public only""" + info["role"][info["role"].index(role) + 1] + """ shipped(_lotId){

                for(uint i = 0;i<_lotId.length;i++){
                    lots[_lotId[i]].productState = State.Received;
                }
                emit Received();
            }"""
        writeToFile(match_string, insert_string)
    else:
        return


def addMakePack(role, info):
    global contents
    if info["role"].index(role) == 0:
        match_string = "//*addMakePack"
        insert_string = """
            function makeLot(string memory _lotId, string[] memory _productIds) public only""" + role + """{
                
                for(uint i = 0;i<_productIds.length;i++){
                    product memory productDetails = product({
                        productId : _productIds[i],
                        lotId : _lotId,
                        finalBuyingPrice : 0,
                        finalSellingPrice : 0
                    });
                    products[_productIds[i]] = productDetails;
                }
                
                lot memory lotDetails = lot({
                    lotId : _lotId,
                    // factoryId : _factoryId,
                    currentOwner : msg.sender,
                    productIds : _productIds,
                    productState : State.Made
                });
                
                lots[_lotId] = lotDetails;
                lots[_lotId].trackUser[0] = contractOwner;
                lots[_lotId].trackUser[1] = msg.sender;
                emit LotMade(_lotId);
            }
            
            function packedLot(string memory _lotId) public only""" + role + """ made(_lotId){
                
                lots[_lotId].productState = State.Packed;
                emit Packed();
            }"""
        writeToFile(match_string, insert_string)
    else:
        return


def convert(s): 
    if(len(s) == 0): 
        return
    s1 = '' 
    s1 += s[0].upper() 
    for i in range(1, len(s)): 
        if (s[i] == ' '): 
            s1 += s[i + 1].upper() 
            i += 1
        elif(s[i - 1] != ' '): 
            s1 += s[i]  
    return s1




# info = {
#     "name" : "Product trial Supply Chain",
#     "role" : ["Manufacturer", "Distributer", "Retailer"]
# }



def generateContract(info):
    global contents
    doc = open("SupplyChainTemplate.sol", 'r+')
    contents = doc.readlines()
    doc.close()

    for role in info["role"]:
        addEvent(role)
        addModifier(role, info)
        addRole(role, info)
        addMakePack(role, info)
        addForSale(role, info)
        addPayFrom(role, info)
        addSellTo(role, info)
        addShipLot(role, info)
        addReceivedBy(role, info)


    nameOfDoc = convert(info["name"]) + ".sol"
    newDoc = open(nameOfDoc, 'w')
    newDoc.seek(0)
    newDoc.writelines(contents)
    newDoc.close()


