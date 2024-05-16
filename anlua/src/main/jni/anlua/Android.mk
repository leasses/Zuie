LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE     		:= anlua
LOCAL_SRC_FILES  		:= anlua.c
LOCAL_SHARED_LIBRARIES 	:= liblogger
LOCAL_STATIC_LIBRARIES 	:= libluajit

include $(BUILD_SHARED_LIBRARY)