LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE     		:= jua
LOCAL_SRC_FILES  		:= jua.c
LOCAL_SHARED_LIBRARIES 	:= liblogger
LOCAL_STATIC_LIBRARIES 	:= libluajit

include $(BUILD_SHARED_LIBRARY)