pragma solidity ^0.5.17;
pragma experimental ABIEncoderV2;

contract SupplyChain{
    
    address payable contractOwner;
    mapping(string => lot) lots;            //mapping of lotId with struct of lot
    mapping(string => product) products;    //mapping of productId with struct of product
    mapping(address => user) users;         //mapping of userAddress with struct of user
    mapping(address => bool) isUser;        // checks if he is part of supplychain
    mapping(string => deal) deals;          //mapping of txnHash with struct of deal
    address[] allUserAddress;

    enum State{
        Assembling,
        Made,
        Packed,
        ForSale,
        Sold,
        Shipped,
        Received
    }
    
    State constant defaultState = State.Assembling;
    
//*addRolesArray

    
    string companyName;
    string productName;
    string productCategory;
    
    struct user{
        uint role;
        address userId;
        address parentId;           //stores the address of parent i.e. distributer will have manufacturer as his parent
        address[] childIds;         // array of its children addresses
        uint currentQuantity;
        string name;
        string latitude;
        string longitude;
    }
    
    struct deal{ 
        string txnHash;
        uint capacity;
        address payable buyerAddress;
        uint buyingPrice;
        address sellerAddress;
        uint sellingPrice;
    }

    struct lot{ 
        string lotId;
        address currentOwner;
        string[] productIds;
        State productState;
        address[] trackUser;
        uint[] buyingPrices;
        uint[] sellingPrices;
        string[] trackTxn;

    }
    
    struct product{
        string productId;       //Barcode
        string lotId;
        // uint finalBuyingPrice;
        uint finalSellingPrice;
    }
    
    event Made(string lotId);
    event Packed();
    event ForSale();
    event Sold();
    event Shipped();
    event Received();
    event TxAdded();
    
    event OwnerGivenNextRole();
    event UserAdded(address indexed account);
    event UserDetailsAdded(address sender);

    event LotMade(string lotId);
    
    event PaymentSuccessful();
    event DealFailed(address buyerAddress);
    
    constructor(string memory _companyName, string memory _productName, string memory _productCategory, string memory _name, string memory _latitude, string memory _longitude) public {
        require(bytes(_companyName).length!=0 && bytes(_productName).length!=0 && bytes(_name).length!=0 && bytes(_latitude).length!=0 && bytes(_longitude).length!=0,"All parameters are compulsory!");
        companyName = _companyName;
        productName = _productName;
        productCategory = _productCategory;
        contractOwner = msg.sender;
        address[] memory _childIds;
        user memory owner = user({
            role: 0,
            userId : msg.sender,
            parentId : address(0),
            currentQuantity : 0,
            name: _name,
            childIds: _childIds,
            latitude: _latitude,
            longitude: _longitude
        });
        setUser(msg.sender);
        users[msg.sender] = owner;
    }
    
    modifier onlyContractOwner{
        
        require(msg.sender == contractOwner);
        _;
    }
    
    modifier onlyManufacturer{
        
        require(users[msg.sender].role == 1);
        _;
    }
    
    
    // Define a modifier that checks if the state of a lot is Made
    modifier made(string memory _lotId) {
        
        require(lots[_lotId].productState == State.Made);
        _;
    }
  
    // Define a modifier that checks if the state of multiple lots is Packed
    modifier packed(string[] memory _lotId) {
        
        for(uint i=0; i<_lotId.length;i++)
            require(lots[_lotId[i]].productState == State.Packed);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is ForSale
    modifier forSale(string[] memory _lotId) {

        for(uint i=0; i<_lotId.length;i++)
            require(lots[_lotId[i]].productState == State.ForSale);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is Sold
    modifier sold(string[] memory _lotId) {

        for(uint i=0; i<_lotId.length;i++)
            require(lots[_lotId[i]].productState == State.Sold);
        _;
    }
  
    // Define a modifier that checks if the state of multiple lots is Shipped
    modifier shipped(string[] memory _lotId) {

        for(uint i=0; i<_lotId.length;i++)
            require(lots[_lotId[i]].productState == State.Shipped);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is Received
    modifier received(string[] memory _lotId) {

        for(uint i=0; i<_lotId.length;i++)
            require(lots[_lotId[i]].productState == State.Received);
        _;
    }

    // Define a modifier that checks if the deal was valid
    modifier validDeal(string memory txnHash) {

        require(deals[txnHash].sellingPrice==deals[txnHash].buyingPrice);
        _;
    }
    
    
    function getUserRolesArray() public view returns(string[] memory){
        return UserRoles;
    }

    function getOwner() public view returns(address){

        return contractOwner;
    }
    
    function checkIsUser(address account) public view returns(bool){
        
        return isUser[account];
    }
    
    function getActualUserRole(address account) internal view returns(uint){

        if(account==contractOwner)
            return 0;
        if(checkIsUser(account))
            return users[account].role;
        else
            return uint(UserRoles.length-1);
    }
    
    function getUserRole(address account) public view returns(uint){

        if(checkIsUser(account))
            return users[account].role;
        else
            return uint(UserRoles.length-1);
    }
    
    function getSmartContractDetails() public view returns(string memory, string memory, string memory, string memory, address){

        require(checkIsUser(msg.sender),"You can't access this data!");
        return (
            companyName,
            productName,
            productCategory,
            users[contractOwner].name,
            contractOwner
        );
    }

    function getUserDetails(address account) public view returns(uint, string memory, string memory, string memory, address, address[] memory, uint){
        return (
            getUserRole(account),
            users[account].name,
            users[account].longitude,
            users[account].latitude,
            users[account].parentId,
            users[account].childIds,
            users[account].currentQuantity
        );
    }

    function setUser(address account) internal{
        
        isUser[account]=true;
        allUserAddress.push(account);
    }

    
    function makeOwnerAsNextRole() public onlyContractOwner{
        
        if(users[msg.sender].role==0){
            users[msg.sender].role=1;
            emit OwnerGivenNextRole();
        }
    }
    
    function addChildUser(address _userAccount) public {
        
        require(!checkIsUser(_userAccount),"User already exists!");
        address[] memory _childIds;
        user memory child = user({
            role: getActualUserRole(msg.sender)+1,
            userId : _userAccount,
            parentId : msg.sender,
            currentQuantity : 0,
            name: "",
            childIds: _childIds,
            longitude: "",
            latitude: ""
        });
        
        users[_userAccount] = child;
        users[msg.sender].childIds.push(_userAccount);
        setUser(_userAccount);
        emit UserAdded(_userAccount);
    }
    
    function addOtherUser(address _parentAccount, address _userAccount, uint _userRole) public {
        
        require(!checkIsUser(_userAccount),"User already exists!");
        require(getUserRole(msg.sender)<_userRole, "You don't have permission!");
        if(_parentAccount!=contractOwner)
            require(getActualUserRole(_parentAccount)==_userRole-1, "Given parent doesn't have enough permission!");
        else{
            require(_userRole==2, "Given user doesn't have correct permission!");
            makeOwnerAsNextRole();
        }
        address[] memory _childIds;
        user memory child = user({
            role: _userRole,
            userId : _userAccount,
            parentId : _parentAccount,
            currentQuantity : 0,
            name: "",
            childIds: _childIds,
            longitude: "",
            latitude: ""
        });
        
        users[_userAccount] = child;
        users[_parentAccount].childIds.push(_userAccount);
        setUser(_userAccount);
        emit UserAdded(_userAccount);
    }
    
    function setUserDetails(string memory _name, string memory _latitude, string memory _longitude) public {
        
        require(checkIsUser(msg.sender),"You are not a part of this Supply-Chain");
        require(bytes(users[msg.sender].name).length==0,"You have added your details already!");
        users[msg.sender].name = _name;
        users[msg.sender].latitude = _latitude;
        users[msg.sender].longitude = _longitude;
        emit UserDetailsAdded(msg.sender);
    }

    function makeLot(string memory _lotId, string[] memory _productIds) public onlyManufacturer{
        
        for(uint i = 0;i<_productIds.length;i++){
            product memory productDetails = product({
                productId : _productIds[i],
                lotId : _lotId,
                // finalBuyingPrice : 0,
                finalSellingPrice : 0
            });
            products[_productIds[i]] = productDetails;
        }
        
        address[] memory _trackUser;
        uint[] memory _buyingPrices;
        uint[] memory _sellingPrices;
        string[] memory _trackTxn;
        lot memory lotDetails = lot({
            lotId : _lotId,
            currentOwner : msg.sender,
            productIds : _productIds,
            productState : State.Made,
            trackUser : _trackUser,
            buyingPrices: _buyingPrices,
            sellingPrices: _sellingPrices,
            trackTxn : _trackTxn
        });
        
        lots[_lotId] = lotDetails;
        lots[_lotId].trackUser.push(contractOwner);
        lots[_lotId].trackUser.push(msg.sender);
        lots[_lotId].buyingPrices.push(0);
        lots[_lotId].buyingPrices.push(0);
        lots[_lotId].sellingPrices.push(0);
        lots[_lotId].trackTxn.push("null");
        lots[_lotId].trackTxn.push("null");
        emit LotMade(_lotId);
    }
    
    function packLot(string memory _lotId) public onlyManufacturer made(_lotId){
        
        lots[_lotId].productState = State.Packed;
        emit Packed();
    }
    
    function forSaleLotByManufacturer(string[] memory _lotId, uint _unitPrice) public onlyManufacturer packed(_lotId){
        
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].sellingPrices.push(_unitPrice);
            lots[_lotId[i]].productState = State.ForSale;
        }
        users[msg.sender].currentQuantity+=_lotId.length;
        emit ForSale();
    }
    
    function PayForOrder( string memory _txnHash, uint _quantity, uint _totalBuyingPrice, address _X_Address) public{
        
        require(getUserRole(msg.sender)-getUserRole(_X_Address)==1,"You haven't paid your parent user!");
        require(_quantity<=users[users[msg.sender].parentId].currentQuantity, "Seller doesn't have enough quantity to satisfy your needs!");
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
    }

    function SellLot(string memory _txnHash, uint _totalSellingPrice, string[] memory _lotId) public forSale(_lotId){
        
        deal storage activeDeal = deals[_txnHash];
        require(msg.sender==activeDeal.sellerAddress,"You don't have required permission!");
        if(activeDeal.buyingPrice!=_totalSellingPrice || _lotId.length!=activeDeal.capacity){
            activeDeal.buyerAddress.transfer(activeDeal.buyingPrice);
            emit DealFailed(activeDeal.buyerAddress);
            require(activeDeal.buyingPrice==_totalSellingPrice,"You are not selling at correct price!");
            require(_lotId.length==activeDeal.capacity,"You are not selling the correct quantity!");
        }else{
            activeDeal.sellingPrice = _totalSellingPrice;
            deals[_txnHash] = activeDeal;
            for(uint i = 0;i<_lotId.length;i++){
                lots[_lotId[i]].buyingPrices.push(activeDeal.buyingPrice/activeDeal.capacity);
                lots[_lotId[i]].currentOwner = activeDeal.buyerAddress;
                lots[_lotId[i]].productState = State.Sold;
            }
            emit Sold();
        }
    }
    
    function ShipLot(string memory _txnHash, string[] memory _lotId) public validDeal(_txnHash) sold(_lotId){
         
        require(msg.sender==deals[_txnHash].sellerAddress);
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].trackUser.push(deals[_txnHash].buyerAddress);
            lots[_lotId[i]].trackTxn.push(_txnHash);
            lots[_lotId[i]].productState = State.Shipped;
        }
        emit Shipped();
    }

    function ReceivedLot(string memory _txnHash, string[] memory _lotId) public validDeal(_txnHash) shipped(_lotId){

        require(msg.sender==deals[_txnHash].buyerAddress);
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].productState = State.Received;
        }
        emit Received();
    }

    function ForSaleLot(string[] memory _lotId, uint _price) public received(_lotId){
                
        require(users[msg.sender].currentQuantity>=_lotId.length);
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].sellingPrices.push(_price/_lotId.length);
            lots[_lotId[i]].productState = State.ForSale;
        }
        emit ForSale();
    }

    function SetProductFinalSellingPrice(string memory _productId, uint sellingPrice) public {
        
        require(getActualUserRole(msg.sender)==uint(UserRoles.length-1), "You don't have enough permission!");
        products[_productId].finalSellingPrice = sellingPrice;
    }
    

    function trackProductByProductId(string memory _productId) public view returns(string memory, uint, address[] memory, uint[] memory,uint[] memory, string[] memory){
        
        string storage _lotId = products[_productId].lotId;
        require(bytes(_lotId).length!=0, "Product is unidentified!");
        return (
            _lotId,
            uint(lots[_lotId].productState),
            lots[_lotId].trackUser,
            lots[_lotId].buyingPrices,
            lots[_lotId].sellingPrices,
            lots[_lotId].trackTxn
        );
    }

    function trackProductByLotId(string memory _lotId) public view returns(string[] memory, uint, address[] memory, uint[] memory,uint[] memory, string[] memory){
        
        return (
            lots[_lotId].productIds,
            uint(lots[_lotId].productState),
            lots[_lotId].trackUser,
            lots[_lotId].buyingPrices,
            lots[_lotId].sellingPrices,
            lots[_lotId].trackTxn
        );
    }


    function ReturnAllUsers() public view returns(address[] memory){
        return (
            allUserAddress
        );
    }
    
//*addSelfDestruct

}
