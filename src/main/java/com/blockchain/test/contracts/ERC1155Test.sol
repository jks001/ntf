// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC1155/presets/ERC1155PresetMinterPauser.sol";

contract ERC1155Test is ERC1155PresetMinterPauser {
    
    uint256 public constant spider_id = 1000;
    uint256 public constant king_id = 3000;
    uint256 public constant tiger_id = 5000;
    uint256 public constant monkey_id = 7000;
    
    /**
    * mint( address to, uint256 id, uint256 amount, bytes memory data )
    * to : miner 地址
    * id : 代币的tokenId
    * amount : 代币的发行数量
    * data : 附加数据，它在ERC1155 规范中并没有指定用途，因此这个参数可以根据自己的需要來传递任何数据，如果没有传空值即可。
    * 本示例共发行了4种代币，其中
    */

    constructor() ERC1155PresetMinterPauser("") {
        _mint(msg.sender, spider_id, 10**18, ""); //spider发行10的18次方个
        _mint(msg.sender, king_id, 1, ""); //king只发行一个，这相当于是一个NFT,其它如spider、tiger 都是同质化token/代币
        _mint(msg.sender, tiger_id, 5, ""); //tiger发行5个
        _mint(msg.sender, monkey_id, 10, ""); //monkey发行10个
    }


    
}