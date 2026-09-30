/*
 * Copyright (C) 2022 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "UdfpsHandler.xiaomi_kona"

#include "UdfpsHandler.h"

#include <android-base/logging.h>
#include <android-base/unique_fd.h>
#include <fcntl.h>
#include <poll.h>
#include <sys/ioctl.h>
#include <thread>
#include <unistd.h>

#define COMMAND_NIT 10
#define PARAM_NIT_FOD 1
#define PARAM_NIT_NONE 0

#define TOUCH_FOD_ENABLE 10
#define TOUCH_IOC_SETMODE 0x5400
#define FOD_STATUS_ON 1
// FocalTech ignores negative values; 100 means no fingerprint operation.
#define FOD_STATUS_OFF 100

static const char* kFodUiPaths[] = {
        "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/fod_ui",
        "/sys/devices/platform/soc/soc:qcom,dsi-display/fod_ui",
};

static const char* kFodStatusPaths[] = {
        "/sys/touchpanel/fod_status",
};

static bool readBool(int fd) {
    char c;
    int rc;

    rc = lseek(fd, 0, SEEK_SET);
    if (rc) {
        LOG(ERROR) << "failed to seek fd, err: " << rc;
        return false;
    }

    rc = read(fd, &c, sizeof(char));
    if (rc != 1) {
        LOG(ERROR) << "failed to read bool from fd, err: " << rc;
        return false;
    }

    return c != '0';
}

class XiaomiKonaUdfpsHandler : public UdfpsHandler {
  public:
    void init(fingerprint_device_t *device) {
        mDevice = device;
        mTouchFd.reset(open("/dev/xiaomi-touch", O_RDWR | O_CLOEXEC));
        if (mTouchFd.get() >= 0) {
            setFodStatus(FOD_STATUS_OFF);
        }

        std::thread([this]() {
            int fd;
            for (auto& path : kFodUiPaths) {
                fd = open(path, O_RDONLY);
                if (fd >= 0) {
                    break;
                }
            }

            if (fd < 0) {
                LOG(ERROR) << "failed to open fd, err: " << fd;
                return;
            }

            int fodStatusFd;
            for (auto& path : kFodStatusPaths) {
                fodStatusFd = open(path, O_RDWR);
                if (fodStatusFd >= 0) {
                    break;
                }
            }

            struct pollfd fodUiPoll = {
                    .fd = fd,
                    .events = POLLERR | POLLPRI,
                    .revents = 0,
            };

            while (true) {
                int rc = poll(&fodUiPoll, 1, -1);
                if (rc < 0) {
                    LOG(ERROR) << "failed to poll fd, err: " << rc;
                    continue;
                }

                mDevice->extCmd(mDevice, COMMAND_NIT,
                                readBool(fd) ? PARAM_NIT_FOD : PARAM_NIT_NONE);
                if (fodStatusFd >= 0) {
                    write(fodStatusFd, readBool(fd) ? "1" : "0", 1);
                }
            }
        }).detach();
    }

    void onFingerDown(uint32_t /*x*/, uint32_t /*y*/, float /*minor*/, float /*major*/) {
        // nothing
    }

    void onFingerUp() {
        // nothing
    }

    void onAcquired(int32_t /*result*/, int32_t vendorCode) {
        // Goodix reports waiting for authentication as 21.
        if (vendorCode == 21) {
            setFodStatus(FOD_STATUS_ON);
        }
    }

    void onAuthenticationSucceeded() {
        setFodStatus(FOD_STATUS_OFF);
    }

    void cancel() {
        setFodStatus(FOD_STATUS_OFF);
    }
  private:
    void setFodStatus(int status) {
        if (mTouchFd.get() < 0) {
            return;
        }
        // The driver copies a full MAX_BUF_SIZE array in both directions.
        int args[256] = {TOUCH_FOD_ENABLE, status};
        if (ioctl(mTouchFd.get(), TOUCH_IOC_SETMODE, args) < 0) {
            PLOG(ERROR) << "failed to set touchscreen FOD status: " << status;
        }
    }

    fingerprint_device_t *mDevice;
    android::base::unique_fd mTouchFd;
};

static UdfpsHandler* create() {
    return new XiaomiKonaUdfpsHandler();
}

static void destroy(UdfpsHandler* handler) {
    delete handler;
}

extern "C" UdfpsHandlerFactory UDFPS_HANDLER_FACTORY = {
    .create = create,
    .destroy = destroy,
};
