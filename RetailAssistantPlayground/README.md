Here is the Slang Retail Assistant integration document for detailed reference: https://docs.slanglabs.in/slang/getting-started/integrating-slang-retail-assistant

Please get your API_KEY and ASSISTANT_ID from conva.slanglabs.in console and replace it in the code while initialising Slang Retail Assistant in MainActivity.

## Using the RetailAssistantPlayground
The first time a user clicks on the Slang trigger, Slang undergoes an onboarding flow, where Slang requests and obtains permission from the user and asks the user to choose their language. After the user chooses the language Slang then asks the initial domain specific prompt. This portion of the flow will be the same on your app and in the playground.

After the user speaks something and slang triggers a call back based on the user journey inferred, the App is expected to perform its business logic and pass a return value to Slang. More of which can be read [here](https://docs.slanglabs.in/slang/getting-started/integrating-slang-retail-assistant/supported-user-journeys/search#supported-appstate-conditions)

In order to simulate the return values from the callbacks, these buttons can be clicked. The corresponding prompts will be spoken and action taken as if the app had sent them.
