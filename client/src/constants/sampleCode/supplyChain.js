export const code = `pragma solidity ^0.5.17;
pragma experimental ABIEncoderV2;

contract SupplyChain{
    
    address contractOwner;
    mapping(string => lot) lots;            //mapping of lotId with struct of lot
    mapping(string => product) products;    //mapping of productId with struct of product
    mapping(address => user) users;         //mapping of userAddress with struct of user
    mapping(address => bool) isUser;        // checks if he is part of supplychain
    mapping(string => deal) deals;          //mapping of txnHash with struct of deal

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
    string brandName;
    
    struct user{
        uint role;
        address userId;
        address parentId;                   //stores the address of parent i.e. distributer will have manufacturer as his parent
        mapping(address=>bool) childIds;    // mapping of current user to its children addresses
        uint currentQuantity;
        string name;
        string officeAddress;
    }
    
    /*struct factory{
        string factoryId;
        string  originFactoryName;          // Manufacturer Name
        string  originFactoryInformation;   // Manufacturer Information
        string  originFactoryAddress;       // Factory Address
    }*/
    
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
        // string factoryId;
        address currentOwner;
        string[] productIds;
        State productState;
        mapping(uint => address) trackUser;         //Roles mapped with userId [0->Owner, 1-> Manufacturer, 2-> Distributer, 3-> Retailer]
        mapping(address => uint) buyingPrices;      //above userId mapped to buyingPrices
        mapping(address => uint) sellingPrices;     //above userId mapped to sellingPrices
        mapping(uint => string) trackTxn;           //Roles mapped with userId [0->Owner, 1-> Manufacturer, 2-> Distributer, 3-> Retailer]
    }
    
    
    struct product{
        string productId;       //Barcode
        string lotId;
        uint finalBuyingPrice;
        uint finalSellingPrice;
    }
    
    event Made(string lotId);
    event Packed();
    event ForSale();
    event Sold();
    event Shipped();
    event Received();
    event TxAdded();
    
    event OwnerMadeManufacturer();
    event UserAdded(address indexed account);
    // event ManufacturerAdded(address indexed account);
    // event ManufacturerRemoved(address indexed account);
    // event DistributorAdded(address indexed account);
    event DistributorRemoved(address indexed account);
    event RetailerAdded(address indexed account);
    event RetailerRemoved(address indexed account);
    
    // event FactoryAdded(string factoryId);
    event LotMade(string lotId);
    
    event PaymentSuccessful();
    event DealFailed(address buyerAddress);
    
    constructor(string memory _name, string memory _officeAddress) public {
        require(bytes(_name).length!=0 && bytes(_officeAddress).length!=0);
        contractOwner = msg.sender;
        user memory owner = user({
            role: 0,
            userId : msg.sender,
            parentId : address(0),
            currentQuantity : 0,
            name: _name,
            officeAddress: _officeAddress
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
    
    function checkIsUser(address account) internal view returns(bool){
        
        return isUser[account];
    }
    
    function getUserRole(address account) public view returns(uint){

        if(account==contractOwner)
            return 0;
        if(checkIsUser(account))
            return users[account].role;
        else
            return 4;
    }

    function getUserDetails(address account) public view returns(string memory, string memory){
            
        return (
                users[account].name,
                users[account].officeAddress
            );
    }

    function setUser(address account) internal{
        
        isUser[account]=true;
    }

  /*  function removeUser(address account) internal{
        users[users[account].parentId].childIds[account]=false;
        users[account].chi
        isUser[account]=false;
    }
    */
    
    function makeOwnerAsManufacturer() public onlyContractOwner{
        
        if(users[msg.sender].role==0){
            users[msg.sender].role=1;
            emit OwnerMadeManufacturer();
        }
    }
    
    function addChildUser(string memory _name, string memory _officeAddress, address _userAccount) public {
        
        require(!checkIsUser(_userAccount));
        user memory child = user({
            role: getUserRole(msg.sender)+1,
            userId : _userAccount,
            parentId : msg.sender,
            currentQuantity : 0,
            name: _name,
            officeAddress: _officeAddress
        });
        
        users[_userAccount] = child;
        users[msg.sender].childIds[_userAccount] = true;
        setUser(_userAccount);
        emit UserAdded(_userAccount);
    }
    
    function addOtherUser(string memory _name, string memory _officeAddress, address _parentAccount, address _userAccount, uint _userRole) public {
        
        require(!checkIsUser(_userAccount));
        require(getUserRole(msg.sender)<_userRole);
        if(_parentAccount!=contractOwner)
            require(getUserRole(_parentAccount)==_userRole-1);
        else{
            require(_userRole==2);
            makeOwnerAsManufacturer();
        }
        user memory child = user({
            role: _userRole,
            userId : _userAccount,
            parentId : _parentAccount,
            currentQuantity : 0,
            name: _name,
            officeAddress: _officeAddress
        });
        
        users[_userAccount] = child;
        users[_parentAccount].childIds[_userAccount] = true;
        setUser(_userAccount);
        emit UserAdded(_userAccount);
    }
    
   
    
    function makeLot(string memory _lotId, string[] memory _productIds) public onlyManufacturer{
        
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
    
    function packedLot(string memory _lotId) public onlyManufacturer made(_lotId){
        
        lots[_lotId].productState = State.Packed;
        emit Packed();
    }
    
    function forSaleLotByManufacturer(string[] memory _lotId, uint _price) public onlyManufacturer packed(_lotId){
        
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].sellingPrices[msg.sender] = _price;
            lots[_lotId[i]].productState = State.ForSale;
        }
        users[msg.sender].currentQuantity+=_lotId.length;
        emit ForSale();
    }
    
    function payFromDistributorToManufacturer(uint _quantity, uint _totalBuyingPrice, string memory _txnHash) public onlyDistributor{
        
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
    }

    function sellLotToDistributor(string memory _txnHash, uint _totalSellingPrice, string[] memory _lotId) public onlyManufacturer forSale(_lotId){
        
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
    }
    
    function shipLotFromManufacturerToDistributor(string[] memory _lotId, string memory _txnHash) public onlyManufacturer validDeal(_txnHash) sold(_lotId){
         
        require(msg.sender!=deals[_txnHash].buyerAddress);
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].trackUser[2] = deals[_txnHash].buyerAddress;
            lots[_lotId[i]].trackTxn[2] = _txnHash;
            lots[_lotId[i]].productState = State.Shipped;
        }
        emit Shipped();
    }

    function receivedByDistributor(string[] memory _lotId) public onlyDistributor shipped(_lotId){

        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].productState = State.Received;
        }
        emit Received();
    }

    function forSaleLotByDistributor(string[] memory _lotId, uint _price) public onlyDistributor received(_lotId){
                
        require(users[msg.sender].currentQuantity>=_lotId.length);
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].sellingPrices[msg.sender] = _price;
            lots[_lotId[i]].productState = State.ForSale;
        }
        emit ForSale();
    }

    function payFromRetailerToDistributor(uint _quantity, uint _totalBuyingPrice, string memory _txnHash) public onlyRetailer{
        
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
    }

    function sellLotToRetailer(string memory _txnHash, uint _totalSellingPrice, string[] memory _lotId) public onlyDistributor forSale(_lotId){
        
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
                lots[_lotId[i]].currentOwner = activeDeal.buyerAddress;
                lots[_lotId[i]].productState = State.Sold;
            }
            emit Sold();
        }
    }
    
    function shipLotFromDistributorToRetailer(string[] memory _lotId, string memory _txnHash) public onlyDistributor validDeal(_txnHash) sold(_lotId){
        
        require(msg.sender!=deals[_txnHash].buyerAddress);
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].trackUser[3] = deals[_txnHash].buyerAddress;
            lots[_lotId[i]].trackTxn[3] = _txnHash;
            lots[_lotId[i]].productState = State.Shipped;
        }
        emit Shipped();
    }

    function receivedByRetailer(string[] memory _lotId) public onlyRetailer shipped(_lotId){

        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].productState = State.Received;
        }
        emit Received();
    }

    function getProductDetails(string memory _productId) public view returns(product memory){

        return products[_productId];
    }

    function setProductFinalSellingPrice(string memory _productId, uint sellingPrice) public {
        
        require(getUserRole(msg.sender)==3);
        products[_productId].finalSellingPrice = sellingPrice;
    }
    
    function setProductFinalBuyingPrice(string memory _productId, uint buyingPrice) public {
        
        require(getUserRole(msg.sender)==4);
        products[_productId].finalBuyingPrice = buyingPrice;
    }

    function trackProductByProductId(string memory _productId) public view returns(uint, address[] memory, uint){
        
        string storage _lotId = products[_productId].lotId;
        return trackProductByLotId(_lotId);
    }

    function trackProductByLotId(string memory _lotId) public view returns(uint, address[] memory, uint){
        
        address[] memory ret = new address[](4);
        uint n = users[lots[_lotId].currentOwner].role;
        for (uint i=0; i<=n; i++) {
            ret[i]=lots[_lotId].trackUser[i];
        }
        return (
            n,
            ret,
            uint(lots[_lotId].productState)
        );
    }
}`;
