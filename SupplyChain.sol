pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract SupplyChain{
    
    address contractOwner;
    mapping(string => factory) factories;   //mapping of factoryId with struct of factory
    mapping(string => lot) lots;            //mapping of lotId with struct of lot
    mapping(string => product) products;    //mapping of productId with struct of product
    mapping(address => user) users;         //mapping of userAddress with struct of user
    mapping(address => bool) isUser;        // checks if he is part of supplychain

    enum State{
        Unbuilt,
        Made,
        Packed,
        ForSale,
        Sold,
        Shipped,
        Received,
        Purchased
    }
    
    State constant defaultState = State.Unbuilt;
    string brandName;
    
    struct user{
        uint role;
        address userId;
        address parentId;     //stores the address of parent i.e. distributer will have manufacturer as his parent
        mapping(address=>bool) childIds;   // mapping of current user to its children addresses
    
    }
    
    struct factory{
        string factoryId;
        string  originFactoryName;          // Manufacturer Name
        string  originFactoryInformation;   // Manufacturer Information
        string  originFactoryLatitude;      // Factory Latitude
        string  originFactoryLongitude;     // Factory Latitude
        
    }
    
    struct lot{ 
        string lotId;
        string factoryId;
        address currentOwner;
        string[] productIds;
        State productState;
        mapping(uint => address) trackUser;         //Roles mapped with userId [0->Owner, 1-> Manufacturer, 2-> Distributer, 3-> Retailer]
        mapping(address => uint) buyingPrices;      //above userId mapped to buyingPrices
        mapping(address => uint) sellingPrices;     //above userId mapped to sellingPrices
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
    
    event ManufacturerAdded(address indexed account);
    event ManufacturerRemoved(address indexed account);
    event DistributorAdded(address indexed account);
    event DistributorRemoved(address indexed account);
    event RetailerAdded(address indexed account);
    event RetailerRemoved(address indexed account);
    
    event FactoryAdded(string factoryId);
    event LotMade(string lotId);
    
    constructor() public {
    
        contractOwner = msg.sender;
        user memory owner = user({
            role: 0,
            userId : msg.sender,
            parentId : address(0)
        });
        
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
    
    // Define a modifier that checks if the state of a lot is Made
    modifier made(string memory _lotId) {
        
        require(lots[_lotId].productState == State.Made);
        _;
    }
  
    // Define a modifier that checks if the state of multiple lots is Packed
    modifier packed(string[] memory _lotId) {
        
        for(uint i =0; i<_lotId.length;i++)
            require(lots[_lotId[i]].productState == State.Packed);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is ForSale
    modifier forSale(string[] memory _lotId) {

        for(uint i =0; i<_lotId.length;i++)
            require(lots[_lotId[i]].productState == State.ForSale);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is Sold
    modifier sold(string memory _lotId) {

        require(lots[_lotId].productState == State.Sold);

        _;
    }
  
    // Define a modifier that checks if the state of multiple lots is Shipped
    modifier shipped(string memory _lotId) {

        require(lots[_lotId].productState == State.Shipped);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is Received
    modifier received(string memory _lotId) {

        require(lots[_lotId].productState == State.Received);
        _;
    }
    
    function checkIsUser(address account) internal view returns(bool){
        
        return isUser[account];
    }
    
    function getRole(address account) public view returns(int){

        if(account==contractOwner)
            return 0;
        if(checkIsUser(account))
            return int(users[account].role);
        else
            return -1;
    }
    
    function setUser(address account) internal{
        
        isUser[account]=true;
    }

    function removeUser(address account) internal{

        users[users[account].parentId].childIds[account]=false;
        isUser[account]=false;
    }
    
    function addManufacturer(address account) public onlyContractOwner{
        
        require(!checkIsUser(account));
        user memory manufacturer = user({
            role: 1,
            userId : account,
            parentId : msg.sender
        });
        
        users[account] = manufacturer;
        users[msg.sender].childIds[account] = true;
        setUser(account);
        emit ManufacturerAdded(account);
    }
    
    
    function removeManufacturer(address account) public onlyContractOwner{
        
        require(isUser[account]);
        require(users[account].role == 1);
        removeUser(account);
        delete users[account];
        emit ManufacturerRemoved(account);
    }
    
    function addDistributor(address account) public onlyManufacturer{
        
        require(!checkIsUser(account));
        user memory distributor = user({
            role: 2,
            userId : account,
            parentId : msg.sender
        });
        users[account] = distributor;
        users[msg.sender].childIds[account]=true;
        setUser(account);
        emit DistributorAdded(account);
    }
    
    function removeDistributor(address account) public onlyManufacturer{
        
        require(isUser[account]);
        require(users[account].role == 2);
        removeUser(account);
        delete users[account];
        emit DistributorRemoved(account);
    }
    
    function addRetailer(address account) public onlyDistributor{
        
        require(!checkIsUser(account));
        user memory retailer = user({
            role: 3,
            userId : account,
            parentId : msg.sender
        });
        
        users[account] = retailer;
        users[msg.sender].childIds[account] = true;
        setUser(account);
        emit RetailerAdded(account);
    }
    
    function removeRetailer(address account) public onlyDistributor{
        
        require(isUser[account]);
        require(users[account].role == 3);
        removeUser(account);
        delete users[account];
        emit RetailerRemoved(account);
    }
    

    function addFactoryDetails(string memory _factoryId, string memory _originFactoryName, string memory _originFactoryInformation, string memory _originFactoryLatitude, string memory _originFactoryLongitude) public onlyManufacturer {
        
        factory memory factoryDetails = factory({
           
            factoryId : _factoryId,
            originFactoryName : _originFactoryName,                 // Manufacturer Name
            originFactoryInformation :  _originFactoryInformation,  // Manufacturer Information
            originFactoryLatitude : _originFactoryLatitude,         // Factory Latitude
            originFactoryLongitude : _originFactoryLongitude        // Factory Latitude
        });
        
        factories[_factoryId] = factoryDetails;
        emit FactoryAdded(_factoryId);
    }
    
    function makeLot(string memory _factoryId,string memory _lotId, string[] memory _productIds) public onlyManufacturer{
        
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
            factoryId : _factoryId,
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
    
    function forSaleLot(string[] memory _lotId, uint _price) public onlyManufacturer packed(_lotId){
        
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].sellingPrices[msg.sender] = _price;
            lots[_lotId[i]].productState = State.ForSale;
        }
        emit ForSale();
    }
    
    function sellLot(string[] memory _lotId, address distributorId, uint _price) public onlyManufacturer forSale(_lotId){
        
        require(distributorId!=msg.sender);
        for(uint i = 0;i<_lotId.length;i++){
            lots[_lotId[i]].sellingPrices[msg.sender] = _price;
            lots[_lotId[i]].trackUser[2] = distributorId;
            lots[_lotId[i]].productState = State.Sold;
        }
        emit Sold();
    }

    function getProductDetails(string memory _productId) public view returns(product memory){

        return products[_productId];
    }

    function trackProductByProductId(string memory _productId) public view returns(address[] memory){
        
        string storage _lotId = products[_productId].lotId;
        return trackProductByLotId(_lotId);
    }
    
    function trackProductByLotId(string memory _lotId) public view returns(address[] memory){
        
        address[] memory ret = new address[](4);
        for (uint i = 0; i <= users[lots[_lotId].currentOwner].role; i++) {
            ret[i]=lots[_lotId].trackUser[i];
        }
        return ret;
    }
}
