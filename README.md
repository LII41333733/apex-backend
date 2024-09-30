# Apex Options Trading

Apex Options Trading is a platform that allows traders to open ETF and equity positions supported by personalized risk management strategies ensuring swift order execution and effective profit security.

This application is the culmination of the passion I have for both web development and my interest in the rising influence of Fintech services adopted for retail traders. Industry-leading trading platforms such as Fidelity, WeBull, ThinkorSwim and Robinhood all have their relative strengths, however I wanted to create my own platform where users have better control over risk/reward preferences in the *pre*-trade phase to forego the need to stress or stare at charts throughout the lifetime of the contracts.

### Back-end

The back-end was built on a RESTful API architechture using Java with Spring Boot. It integrates a MySQL database for data persistence, ORM Mapping with Hibernate and JWT to secure the API. The application leverages WebSockets to stream real-time market data to the client.

All trades are placed using the [Tradier Brokerage API](https://documentation.tradier.com/brokerage-api).

### Front-end

The front-end was written in TypeScript using React supported by Vite. State management uses Redux Toolkit; data fetching and caching uses RTK Query.

The UI was designed using components from [shadcn/ui](https://ui.shadcn.com/).
  
### Features
    
- Base Trades
  - Each trade risks a predetermined base percentage of the account's available buying power.
  - When placing a trade, based on the price of each contract, the application calculates the initial stop loss, a first trim and a second trim to be executed based on price action.
  - After execution, the price on each order is watched and will execute a trade when each benchmark is reached.
    - For example, I want to buy 9 contracts of AAPL for 1.20. My preferences employ a 20% stop loss, a first trim at 30% profit and a second trim at 60%.
    - If the contract price reaches 0.96 before the first trim is reached, all contracts are sold.
    - In the event of value increase, when the first trim level is reached, 1/3 of the contracts bought will be sold. At this point, the stop loss is moved to breakeven. Another 1/3 of the contracts will be sold on the second trim.
    - After the second trim is reached, a moving stop loss floor is activated using 20% of the second trim price. When the current price rises, the floor will also, however the stop loss price will remain if the price dips, ensuring profits are secured.

- Lotto Trades
  - Reserved for high-risk, high-reward speculations and uses less leverage to execute these trades.
  - Has a lower stop-loss floor and one less trim than the Base Trade, but handles market volatility better on runner trades to maximize profit on larger price swings.
