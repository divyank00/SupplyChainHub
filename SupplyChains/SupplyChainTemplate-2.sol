pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract SupplyChain {
    address contractOwner;
    // mapping(string => factory) factories;   //mapping of factoryId with struct of factory
    mapping(string => lot) lots; //mapping of lotId with struct of lot
    mapping(string => product) products; //mapping of productId with struct of product
    mapping(address => user) users; //mapping of userAddress with struct of user
    mapping(address => bool) isUser; // checks if he is part of supplychain
    mapping(string => deal) deals; //mapping of txnHash with struct of deal

    enum State {Assembling, Made, Packed, ForSale, Sold, Shipped, Received}

    State constant defaultState = State.Assembling;
    string brandName;

    struct user {
        uint256 role;
        address userId;
        address parentId; //stores the address of parent i.e. distributer will have manufacturer as his parent
        mapping(address => bool) childIds; // mapping of current user to its children addresses
        uint256 currentQuantity;
        string name;
        string officeAddress;
    }

    /*struct factory{
        string factoryId;
        string  originFactoryName;          // Manufacturer Name
        string  originFactoryInformation;   // Manufacturer Information
        string  originFactoryAddress;       // Factory Address
    }*/

    struct deal {
        string txnHash;
        uint256 capacity;
        address buyerAddress;
        uint256 buyingPrice;
        address sellerAddress;
        uint256 sellingPrice;
    }

    struct lot {
        string lotId;
        // string factoryId;
        address currentOwner;
        string[] productIds;
        State productState;
        mapping(uint256 => address) trackUser; //Roles mapped with userId [0->Owner, 1-> Manufacturer, 2-> Distributer, 3-> Retailer]
        mapping(address => uint256) buyingPrices; //above userId mapped to buyingPrices
        mapping(address => uint256) sellingPrices; //above userId mapped to sellingPrices
        mapping(uint256 => string) trackTxn; //Roles mapped with userId [0->Owner, 1-> Manufacturer, 2-> Distributer, 3-> Retailer]
    }

    struct product {
        string productId; //Barcode
        string lotId;
        uint256 finalBuyingPrice;
        uint256 finalSellingPrice;
    }

    event Made(string lotId);
    event Packed();
    event ForSale();
    event Sold();
    event Shipped();
    event Received();
    event TxAdded();

    //*addEvents

    // event FactoryAdded(string factoryId);
    event LotMade(string lotId);

    event PaymentSuccessful();
    event DealFailed(address buyerAddress);

    constructor(string _name, string _officeAddress) public {
        contractOwner = msg.sender;
        user memory owner = user({
            role: 0,
            userId: msg.sender,
            parentId: address(0),
            currentQuantity: 0,
            name: _name,
            officeAddress: _officeAddress
        });

        users[msg.sender] = owner;
    }

    //*addModifiers

    modifier onlyContractOwner {
        require(msg.sender == contractOwner);
        _;
    }

    // Define a modifier that checks if the state of a lot is Made
    modifier made(string memory _lotId) {
        require(lots[_lotId].productState == State.Made);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is Packed
    modifier packed(string[] memory _lotId) {
        for (uint256 i = 0; i < _lotId.length; i++)
            require(lots[_lotId[i]].productState == State.Packed);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is ForSale
    modifier forSale(string[] memory _lotId) {
        for (uint256 i = 0; i < _lotId.length; i++)
            require(lots[_lotId[i]].productState == State.ForSale);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is Sold
    modifier sold(string[] memory _lotId) {
        for (uint256 i = 0; i < _lotId.length; i++)
            require(lots[_lotId[i]].productState == State.Sold);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is Shipped
    modifier shipped(string[] memory _lotId) {
        for (uint256 i = 0; i < _lotId.length; i++)
            require(lots[_lotId[i]].productState == State.Shipped);
        _;
    }

    // Define a modifier that checks if the state of multiple lots is Received
    modifier received(string[] memory _lotId) {
        for (uint256 i = 0; i < _lotId.length; i++)
            require(lots[_lotId[i]].productState == State.Received);
        _;
    }

    // Define a modifier that checks if the deal was valid
    modifier validDeal(string txnHash) {
        require(deals[txnHash].sellingPrice == deals[txnHash].buyingPrice);
        _;
    }

    function checkIsUser(address account) internal view returns (bool) {
        return isUser[account];
    }

    function getUserRole(address account) public view returns (int256) {
        if (account == contractOwner) return 0;
        if (checkIsUser(account)) return int256(users[account].role);
        else return -1;
    }

    function getUserDetails(address account)
        public
        view
        returns (string, string)
    {
        require(checkIsUser(account));
        return (users[account].name, users[account].officeAddress);
    }

    function setUser(address account) internal {
        isUser[account] = true;
    }

    //*addRoles

    //*addMakePack

    //*addForSale

    //*addPayFrom

    //*addSellTo

    //*addShipLot

    //*addReceivedBy

    function getProductDetails(string memory _productId)
        public
        view
        returns (product memory)
    {
        return products[_productId];
    }

    function setProductFinalSellingPrice(
        string memory _productId,
        uint256 sellingPrice
    ) public {
        require(getUserRole(msg.sender) == 3);
        products[_productId].finalSellingPrice = sellingPrice;
    }

    function setProductFinalBuyingPrice(
        string memory _productId,
        uint256 buyingPrice
    ) public {
        require(getUserRole(msg.sender) == -1);
        products[_productId].finalBuyingPrice = buyingPrice;
    }

    function trackProductByProductId(string memory _productId)
        public
        view
        returns (
            uint256,
            address[] memory,
            uint256
        )
    {
        string storage _lotId = products[_productId].lotId;
        return trackProductByLotId(_lotId);
    }

    function trackProductByLotId(string memory _lotId)
        public
        view
        returns (
            uint256,
            address[] memory,
            uint256
        )
    {
        address[] memory ret = new address[](4);
        uint256 n = users[lots[_lotId].currentOwner].role;
        for (uint256 i = 0; i <= n; i++) {
            ret[i] = lots[_lotId].trackUser[i];
        }
        return (n, ret, uint256(lots[_lotId].productState));
    }
}
