import {action, extendObservable} from 'mobx';
import {notifyError, pages} from "../../common/commons";

export default class SignUpStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            emailExists: true,
            signUpDisabled: true,
            timeoutIdToQueryEmail: 0,
            formPasswordDirty: false
        });
    }

    saveUser = (formValues, appRouter) => {
        this.api.saveUser(formValues).then(respBody => {
            appRouter.replace(pages.login);
        });
    };

    // check if email (username field) is already recorded
    // request after 1 seconds when the user finished typing
    handleEmailChange = (props, emailVal) => {

        this.disableSignUp(true);

        if (emailVal) {

            if (this.timeoutIdToQueryEmail) {
                clearTimeout(this.timeoutIdToQueryEmail);
            }

            this.timeoutIdToQueryEmail = setTimeout(function (callback) {
                    callback(emailVal, props.form)
                },
                1000,
                this.queryEmailIfUserStoppedTyping)
        }
    };

    queryEmailIfUserStoppedTyping = (emailVal, formObj) => {

        this.api.queryEmail(emailVal).then(respBody => {

            let exists = respBody === 'true';
            if (exists) {
                notifyError(emailVal + ' is already recorded!');
            }

            this.emailExists = exists;
            this.disableSignUp(exists);

            // force another render after signUpDisabled is updated, because update in action runs latest
            // and MobX does not automatically render it when the value is changed to false for the first time
            formObj.validateFields((err, values) => {
            });
        })
    };

    disableSignUp = action('disableSignUp', (disabled) => {
        this.signUpDisabled = this.emailExists || disabled;
    });

    markPasswordDirty = action('markPasswordDirty', isDirty => {
        this.formPasswordDirty = isDirty;
    });
}