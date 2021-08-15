## React/MobX/AntDesign Example

This project is a simple admin tool demonstration, that is created to learn React and MobX. There is no backend API
since the purpose was only to learn frontend framework.

> Requires yarn 1.5.1 and node 9.11.2 to build and run.

* Run `yarn|npm start` from terminal to start dev server.
    * Use `HTTPS=true yarn start` to start with https enabled.
* Create docker image and run a container with that image:
    * `docker build -t react-mobx-docker-app .`
    * `docker run -it -p 3000:3000 --rm react-mobx-docker-app`

#### How to enable SSL for local development

* react-scripts -> start.js uses webpack-dev-server -> lib -> Server.js, update below part to use custom certificate
  that you also use for your backend server:

```javascript
if (options.https) {
    if (typeof options.https === 'boolean') {
        options.https = {
            key: fs.readFileSync('/SOME_LOCAL_PATH/localhost.key'),
            cert: fs.readFileSync('/SOME_LOCAL_PATH/localhost.crt'),
            ca: fs.readFileSync('/SOME_LOCAL_PATH/localhost.crt'),
            pfx: options.pfx,
            passphrase: options.pfxPassphrase,
            requestCert: options.requestCert || false
        };
    }
}
```

### Pictures

![Login Management][Login Management]

---

![User Admin][User Admin]

---

![SAP Admin][SAP Admin]

[Login Management]: readme-resources/1.jpg "Login Management"

[User Admin]: readme-resources/2.jpg "User Admin"

[SAP Admin]: readme-resources/3.jpg "SAP Admin"