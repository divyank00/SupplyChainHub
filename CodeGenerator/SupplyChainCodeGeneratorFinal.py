def writeToFile(match_string, insert_string):
    global contents
    if match_string in contents[-1]:  # Handle last line to prevent IndexError
        contents.append(insert_string)
    else:
        for index, line in enumerate(contents):
            if match_string in line and insert_string not in contents[index + 1]:
                contents.insert(index + 1, insert_string)
                break


def addRolesArray(info):
    global contents
    match_string = "//*addRolesArray"
    insert_string = """
    string[] UserRoles = """ + str(info["role"]) + """;"""
    writeToFile(match_string, insert_string)

def addSelfDestruct():
    global contents
    match_string = "//*addSelfDestruct"
    insert_string = """
    function kill() public onlyContractOwner{
        selfdestruct(contractOwner);
    }"""
    writeToFile(match_string, insert_string)


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
#     "name" : "Product trial Supply Chain only lot",
#     "role" : ["Owner","Manufacturer", "Distributer", "Retailer", "Customer"],
#     "tracking" : "lotAndProduct",#onlyLot,onlyProduct,lotAndProduct
#     "selfDestruct" : True#or False bool
# }

#string[] UserRoles = ["Owner", "Manufacturer", "Distributor", "Retailer", "Customer"];



def generateContract(info):
    global contents
    if info["tracking"] == "onlyLot":
       doc = open("SupplyChainOnlyLotTemplate.sol", 'r+') 
    elif info["tracking"] == "onlyProduct":
        doc = open("SupplyChainOnlyProductTemplate.sol", 'r+')
    elif info["tracking"] == "lotAndProduct":
        doc = open("SupplyChainLotAndProductTemplate.sol", 'r+')
    
    contents = doc.readlines()
    doc.close()

    addRolesArray(info)
    if info["selfDestruct"]:
        addSelfDestruct()


    nameOfDoc = convert(info["name"]) + ".sol"
    newDoc = open(nameOfDoc, 'w')
    newDoc.seek(0)
    newDoc.writelines(contents)
    newDoc.close()


