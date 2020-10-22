export const functions = (
  currentRoleName,
  nextRoleName,
  previousRoleName,
  type
) => {
  const arrayFirst = [
    {
      name: "makeLot()",
      desc: `Allows ${currentRoleName} to confirm process of making lot of products for the Supply Chain.`,
    },
    {
      name: "packedLot()",
      desc: `Allows ${currentRoleName} to confirm process of packing lot of products for the Supply Chain.`,
    },
    {
      name: `forSaleLotBy${currentRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
    {
      name: `sellLotTo${nextRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
    {
      name: `shipLotFrom${currentRoleName}To${nextRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
    {
      name: `add${nextRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
  ];
  const arrayMiddle = [
    {
      name: `payFrom${currentRoleName}To${previousRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
    {
      name: `receivedBy${currentRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
    {
      name: `forSaleLotBy${currentRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
    {
      name: `sellLotTo${nextRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
    {
      name: `shipLotFrom${currentRoleName}To${nextRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
    {
      name: `add${nextRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
  ];
  const arrayLast = [
    {
      name: `payFrom${currentRoleName}To${previousRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
    {
      name: `receivedBy${currentRoleName}()`,
      desc: `Allows ${currentRoleName} to put lot on sale of making lot of products for the Supply Chain.`,
    },
  ];

  switch (type) {
    case 0:
      return arrayFirst;
    case 1:
      return arrayMiddle;
    case 2:
      return arrayLast;
    default:
      return arrayMiddle;
  }
};
