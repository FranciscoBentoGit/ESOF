<template>
  <v-row>
      <v-col>
        <v-row
          v-for="(leftIcon, index) in questionDetails.leftIcons"
          :key="index"
          data-cy="leftIconsInput"
        >
          <v-col cols="3">
          </v-col>
          <v-col cols="6">
            <v-textarea
              v-model="leftIcon.content"
              :label="`Left Item ${index + 1}`"
              :data-cy="`leftIcons${index + 1}`"
              rows="1"
              auto-grow
              readonly
            ></v-textarea>
          </v-col>
        </v-row>
      </v-col>
      <v-col>
        <v-row
          v-for="(rightIcon, index) in questionDetails.rightIcons"
          :key="index"
          data-cy="rightIconsInput"
        >
            <v-col cols="5">
              <v-select
                multiple="true"
                :label="`Left Item Matches`"
                :items="getAvailableLeftIcons"
                v-model="answerDetails.rightIconIds[index]"
                v-bind:class="['option', optionClass(index)]"
                :data-cy="`matches${index + 1}`"
              />
            </v-col>
            <v-col cols="5"
              v-if="
              isReadonly &&
              correctAnswerDetails.rightIconIds[index] !==
              rightIcon.match
            ">
              <v-select
                multiple="true"
                :label="`Correct Matches`"
                :items="getAvailableLeftIcons"
                v-model="rightIcon.match"
                :data-cy="`matchesAnswer${index + 1}`"
                readonly
                class= "correct-question correct"
              />
            </v-col>
          <v-col 
          cols="6">
            <v-textarea
              v-model="questionDetails.rightIcons[index].content"
              :label="`Right Item ${index + 1}`"
              :data-cy="`rightIcons${index + 1}`"
              rows="1"
              auto-grow
              readonly
            ></v-textarea>
          </v-col>
        </v-row>
      </v-col>
    </v-row>

</template>

<script lang="ts">
import { Component, Vue, Prop, Model, Emit } from 'vue-property-decorator';
import ItemCombinationStatementQuestionDetails from '@/models/statement/questions/ItemCombinationStatementQuestionDetails';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import ItemCombinationStatementAnswerDetails from '@/models/statement/questions/ItemCombinationStatementAnswerDetails';
import ItemCombinationStatementCorrectAnswerDetails from '@/models/statement/questions/ItemCombinationStatementCorrectAnswerDetails';

@Component
export default class ItemCombinationAnswer extends Vue {
  @Prop(ItemCombinationStatementQuestionDetails)
  readonly questionDetails!: ItemCombinationStatementQuestionDetails;
  @Prop(ItemCombinationStatementAnswerDetails)
  answerDetails!: ItemCombinationStatementAnswerDetails;
  @Prop(ItemCombinationStatementCorrectAnswerDetails)
  readonly correctAnswerDetails?:ItemCombinationStatementCorrectAnswerDetails;

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  get isReadonly() {
    return !!this.correctAnswerDetails;
  }

  optionClass(index: number) {
    if (this.isReadonly) {
      if (
        !!this.correctAnswerDetails &&
        this.answerDetails.rightIconIds[index].match ===
          this.questionDetails.rightIcons[index].match
      ) {
        return 'correct';
      } else if (
        this.answerDetails.rightIconIds[index].match !==
        this.questionDetails.rightIcons[index].match
      ) {
        return 'wrong';
      } else {
        return '';
      }
    }
  }

  get getAvailableLeftIcons(): number[] {
    var array = new Array()
    var el = 0;
    for(var i = 0; i < this.questionDetails.leftIcons.length;i++){
        el++;
        array.push(el);  
    }
    return array;
  }
}

</script>

<style lang="scss" scoped>
.unanswered {
  .correct {
    .option-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #333333 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}

.correct-question {
  .correct {
    .option-content {
      background-color: #299455;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #299455 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}

.incorrect-question {
  .wrong {
    .option-content {
      background-color: #cf2323;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #cf2323 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
  .correct {
    .option-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #333333 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}
</style>

