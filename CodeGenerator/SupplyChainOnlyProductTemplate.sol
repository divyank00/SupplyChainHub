pragma solidity ^0.5.17;
pragma experimental ABIEncoderV2;

contract SupplyChain{
    
    address payable contractOwner;
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

    struct product{ 
        string productId;
        address currentOwner;
        uint finalSellingPrice;
        State productState;
        address[] trackUser;
        uint[] buyingPrices;
        uint[] sellingPrices;
        string[] trackTxn;
    }
    
    
    
    
    event Made(string productId);
    event Packed();
    event ForSale();
    event Sold();
    event Shipped();
    event Received();
    event TxAdded();
    
    event OwnerGivenNextRole();
    event UserAdded(address indexed account);
    event UserDetailsAdded(address sender);

    event productMade(string productId);
    
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
    
    modifier onlyDistributor{
       
       require(users[msg.sender].role == 2);
       _;
    }

    modifier onlyRetailer{
       
       require(users[msg.sender].role == 3);
       _;
    }
    
    // Define a modifier that checks if the state of a product is Made
    modifier made(string memory _productId) {
        
        require(products[_productId].productState == State.Made);
        _;
    }
  
    // Define a modifier that checks if the state of multiple products is Packed
    modifier packed(string[] memory _productId) {
        
        for(uint i=0; i<_productId.length;i++)
            require(products[_productId[i]].productState == State.Packed);
        _;
    }

    // Define a modifier that checks if the state of multiple products is ForSale
    modifier forSale(string[] memory _productId) {

        for(uint i=0; i<_productId.length;i++)
            require(products[_productId[i]].productState == State.ForSale);
        _;
    }

    // Define a modifier that checks if the state of multiple products is Sold
    modifier sold(string[] memory _productId) {

        for(uint i=0; i<_productId.length;i++)
            require(products[_productId[i]].productState == State.Sold);
        _;
    }
  
    // Define a modifier that checks if the state of multiple products is Shipped
    modifier shipped(string[] memory _productId) {

        for(uint i=0; i<_productId.length;i++)
            require(products[_productId[i]].productState == State.Shipped);
        _;
    }

    // Define a modifier that checks if the state of multiple products is Received
    modifier received(string[] memory _productId) {

        for(uint i=0; i<_productId.length;i++)
            require(products[_productId[i]].productState == State.Received);
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

    function makeproduct(string memory _productId) public onlyManufacturer{
        
        
        address[] memory _trackUser;
        uint[] memory _buyingPrices;
        uint[] memory _sellingPrices;
        string[] memory _trackTxn;
        product memory productDetails = product({
            productId : _productId,
            currentOwner : msg.sender,
            finalSellingPrice: 0,
            productState : State.Made,
            trackUser : _trackUser,
            buyingPrices: _buyingPrices,
            sellingPrices: _sellingPrices,
            trackTxn : _trackTxn
        });
        
        products[_productId] = productDetails;
        products[_productId].trackUser.push(contractOwner);
        products[_productId].trackUser.push(msg.sender);
        products[_productId].buyingPrices.push(0);
        products[_productId].buyingPrices.push(0);
        products[_productId].sellingPrices.push(0);
        products[_productId].trackTxn.push("");
        products[_productId].trackTxn.push("");
        users[msg.sender].currentQuantity=users[msg.sender].currentQuantity+1;
        emit productMade(_productId);
    }
    
    function packproduct(string memory _productId) public onlyManufacturer made(_productId){
        
        products[_productId].productState = State.Packed;
        emit Packed();
    }
    
    function forSaleproductByManufacturer(string[] memory _productId, uint _price) public onlyManufacturer packed(_productId){
        
        for(uint i = 0;i<_productId.length;i++){
            products[_productId[i]].sellingPrices.push(_price/_productId.length);
            products[_productId[i]].productState = State.ForSale;
        }
        users[msg.sender].currentQuantity+=_productId.length;
        emit ForSale();
    }
    
    function PayForOrder( string memory _txnHash, uint _quantity, uint _totalBuyingPrice, address _Y) public{
        
        require(getActualUserRole(msg.sender)-getActualUserRole(_Y)==1,"You haven't paid your parent user!");
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

    function SellProduct(string memory _txnHash, uint _totalSellingPrice, string[] memory _productId) public forSale(_productId){
        
        deal storage activeDeal = deals[_txnHash];
        require(msg.sender==activeDeal.sellerAddress,"You don't have required permission!");
        if(activeDeal.buyingPrice!=_totalSellingPrice || _productId.length!=activeDeal.capacity){
            activeDeal.buyerAddress.transfer(activeDeal.buyingPrice);
            emit DealFailed(activeDeal.buyerAddress);
            require(activeDeal.buyingPrice==_totalSellingPrice,"You are not selling at correct price!");
            require(_productId.length==activeDeal.capacity,"You are not selling the correct quantity!");
        }else{
            activeDeal.sellingPrice = _totalSellingPrice;
            deals[_txnHash] = activeDeal;
            for(uint i = 0;i<_productId.length;i++){
                products[_productId[i]].buyingPrices.push(activeDeal.buyingPrice/activeDeal.capacity);
                products[_productId[i]].currentOwner = activeDeal.buyerAddress;
                products[_productId[i]].productState = State.Sold;
            }
            emit Sold();
        }
    }
    
    function ShipProduct(string memory _txnHash, string[] memory _productId) public validDeal(_txnHash) sold(_productId){
         
        require(msg.sender==deals[_txnHash].sellerAddress);
        for(uint i = 0;i<_productId.length;i++){
            products[_productId[i]].trackUser.push(deals[_txnHash].buyerAddress);
            products[_productId[i]].trackTxn.push(_txnHash);
            products[_productId[i]].productState = State.Shipped;
        }
        emit Shipped();
    }

    function ReceivedProduct(string memory _txnHash, string[] memory _productId) public validDeal(_txnHash) shipped(_productId){

        require(msg.sender==deals[_txnHash].buyerAddress);
        for(uint i = 0;i<_productId.length;i++){
            products[_productId[i]].productState = State.Received;
        }
        emit Received();
    }

    function ProductForSale(string[] memory _productId, uint _price) public received(_productId){
                
        require(users[msg.sender].currentQuantity>=_productId.length);
        for(uint i = 0;i<_productId.length;i++){
            products[_productId[i]].sellingPrices.push(_price/_productId.length);
            products[_productId[i]].productState = State.ForSale;
        }
        emit ForSale();
    }

    function setProductFinalSellingPrice(string memory _productId, uint sellingPrice) public {
        
        require(getActualUserRole(msg.sender)==uint(UserRoles.length-1), "You don't have enough permission!");
        products[_productId].finalSellingPrice = sellingPrice;
    }

    


    function trackProductByproductId(string memory _productId) public view returns(uint, address[] memory, uint[] memory, uint[] memory, string[] memory){
        
        return (
            uint(products[_productId].productState),
            products[_productId].trackUser,
            products[_productId].buyingPrices,
            products[_productId].sellingPrices,
            products[_productId].trackTxn
        );
    }

    function ReturnAllUsers() public view returns(address[] memory){
        return (
            allUserAddress
        );
    }

//*addSelfDestruct

}