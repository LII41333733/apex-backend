# Apex Options Trading

Apex Options Trading is a platform that allows traders to open ETF and equity positions supported by personalized risk management strategies ensuring swift order execution and effective profit security.

This application is the culmination of the passion I have for both web development and my interest in the rising influence of Fintech services adopted for retail traders. Industry-leading trading platforms such as Fidelity, WeBull, ThinkorSwim and Robinhood all have their relative strengths, however I wanted to create my own platform where users have better control over risk/reward preferences in the *pre*-trade phase to forego the need to stress or stare at charts throughout the lifetime of the contracts.

### Back-end

The back-end was built on a RESTful API architechture using Java with Spring Boot. It integrates a MySQL database for data persistence, ORM Mapping with Hibernate and JWT to secure the API. The application leverages WebSockets to stream real-time market data to the client.

All trades are placed using the [Tradier Brokerage API](https://documentation.tradier.com/brokerage-api).

### Front-end

The front-end was written in TypeScript using React supported by Vite. State management uses Redux Toolkit; aata fetching and caching with RTK Query.

The UI was designed using components from [shadcn/ui](https://ui.shadcn.com/).

## Table of Contents

- [Usage](#usage)
- [Features](#features)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgements](#acknowledgements)
  

## Installation

### Prerequisites

List any prerequisites the user needs before installing, such as programming languages, frameworks, or software.

```bash
# Example
- Node.js >= 12.x
- npm or yarn
- Java >= 11
