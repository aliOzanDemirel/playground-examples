import {notification} from 'antd';

notification.config({
    top: 10,
    placement: 'topRight',
    // duration: 3,
    style: {
        // width: 1500,
        marginRight: 10
    }
});

export function warn(desc, header) {
    notification.warning({
        message: header ? header : 'Warning',
        description: desc
    });
}

export function goodNews(desc, header) {
    notification.success({
        message: header ? header : 'Success',
        description: desc
    });
}

export function badNews(desc, header, duration) {
    notification.error({
        message: header ? header : 'Failed',
        description: desc ? desc : 'No message is returned.',
        duration: typeof duration === 'number' ? duration : 5
    });
}

export function notifyFullTop(desc, header) {
    notification.open({
        message: header,
        description: desc,
        top: 2,
        duration: 5,
        style: {
            width: 1400,
            marginTop: 5
        }
    });
}