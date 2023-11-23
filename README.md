Hello, here is quick guide to what was done and my initial thoughts:
1. I installed on AWS EC2 java 17 and elastic search 7.17.15, enpoints are triggering remote host and works properly 
as per requirements.
2. I chose this version of elastic because it is not old and it is not very fresh to avoid possible bugs. Something stable.
3. I didn't want to use deprecated api, like RestHighLevelClient, TransportClient, my focus was to use something enough fresh too.
4. I don't plan to shut down remote server, so you can check it locally too
5. Most of time took resolving compatibility issues between different dependencies, searching for answers in web.
6. Due to lack of time I didn't add JUNIT testing as I am doing on regular basis
7. I plan to record video proof tomorrow.
8. I found the task interesting, thank you!